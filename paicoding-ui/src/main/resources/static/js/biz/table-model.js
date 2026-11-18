(function (root, factory) {
  const model = factory();
  if (typeof module === 'object' && module.exports) module.exports = model;
  else root.PaiTableModel = model;
})(typeof globalThis !== 'undefined' ? globalThis : this, function () {
  'use strict';

  const uid = prefix => prefix + '_' + (typeof crypto !== 'undefined' && crypto.randomUUID
    ? crypto.randomUUID().replace(/-/g, '')
    : Date.now().toString(36) + Math.random().toString(36).slice(2));

  const field = (name, type) => ({ id: uid('f'), name: String(name || ''), type: type || 'longtext' });

  function safeFilename(name) {
    const cleaned = String(name || '表格').replace(/[\\/:*?"<>|\u0000-\u001f]+/g, ' ').replace(/\s+/g, ' ').trim();
    return (cleaned || '表格').slice(0, 120);
  }

  function csv(doc, records, fields) {
    const activeFields = fields && fields.length ? fields : doc.fields;
    const safe = value => {
      let s = value == null ? '' : String(value);
      if (/^[\s]*[=+\-@]/.test(s)) s = "'" + s;
      return '"' + s.replace(/"/g, '""') + '"';
    };
    const rows = (records || doc.records).map(r => activeFields.map(f => safe(r.values[f.id])).join(','));
    return '\uFEFF' + [activeFields.map(f => safe(f.name)).join(','), ...rows].join('\r\n');
  }

  function parseCsv(input) {
    if (input.length > 1024 * 1024) throw Error('CSV 不能超过 1 MB');
    const text = input.replace(/^\uFEFF/, '');
    const rows = [];
    let row = [], cell = '', quoted = false, closed = false;
    for (let i = 0; i < text.length; i++) {
      const c = text[i];
      if (quoted) {
        if (c === '"') {
          if (text[i + 1] === '"') { cell += '"'; i++; }
          else { quoted = false; closed = true; }
        } else {
          cell += c;
        }
      } else if (c === '"') {
        if (cell || closed) throw Error('CSV 引号格式错误');
        quoted = true;
      } else if (c === ',' || c === '\r' || c === '\n') {
        row.push(cell);
        cell = '';
        closed = false;
        if (c !== ',') {
          rows.push(row);
          row = [];
          if (c === '\r' && text[i + 1] === '\n') i++;
        }
      } else {
        if (closed) throw Error('CSV 引号后存在非法字符');
        cell += c;
      }
      if (rows.length > 1001 || row.length > 30 || cell.length > 4000) {
        throw Error('最多 1000 条记录、30 个字段，每格不超过 4000 字');
      }
    }
    if (quoted) throw Error('CSV 引号未闭合');
    if (cell || closed || row.length) { row.push(cell); rows.push(row); }
    if (!rows.length || !rows[0].length || rows[0].length > 30 || rows.length > 1001) {
      throw Error('CSV 需要表头，最多 30 个字段和 1000 条记录');
    }
    const fields = rows[0].map((name, i) => field((name.trim() || '字段 ' + (i + 1)).slice(0, 60), 'longtext'));
    const records = rows.slice(1).map((cells, i) => {
      if (cells.length !== fields.length) throw Error('第 ' + (i + 2) + ' 行列数与表头不一致');
      const values = Object.create(null);
      fields.forEach((f, n) => {
        if (cells[n].length > 4000) throw Error('单元格超过4000字');
        values[f.id] = cells[n];
      });
      return { id: uid('r'), values };
    });
    return { name: '导入的表格', fields, records };
  }

  return { uid, field, safeFilename, csv, parseCsv };
});
