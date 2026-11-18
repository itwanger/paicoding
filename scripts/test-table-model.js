const assert = require('node:assert/strict');
const M = require('../paicoding-ui/src/main/resources/static/js/biz/table-model.js');

const doc = M.parseCsv('\ufeff任务,备注\r\n集合,"多行\n笔记"\r\n线程池,"他说""你好"""');
assert.equal(doc.records.length, 2);
assert.equal(doc.records[0].values[doc.fields[1].id], '多行\n笔记');
assert.equal(doc.records[1].values[doc.fields[1].id], '他说"你好"');
assert.deepEqual(
  M.parseCsv(M.csv(doc)).records.map(r => Object.values(r.values)),
  doc.records.map(r => Object.values(r.values))
);

// Filtered records + visible fields export
const subsetCsv = M.csv(doc, [doc.records[0]], [doc.fields[0]]);
const subsetDoc = M.parseCsv(subsetCsv);
assert.equal(subsetDoc.fields.length, 1);
assert.equal(subsetDoc.records.length, 1);
assert.equal(subsetDoc.records[0].values[subsetDoc.fields[0].id], '集合');

// Safe filename sanitization
assert.equal(M.safeFilename('Spring AI / MCP : 实战?'), 'Spring AI MCP 实战');

assert.throws(() => M.parseCsv('a,b\n"broken,b'));
assert.throws(() => M.parseCsv('a,b\nx'));
assert.throws(() => M.parseCsv('a\n' + Array(1002).fill('x').join('\n')));

const malicious = M.parseCsv('name\n"=HYPERLINK(""evil"")"');
assert.match(M.csv(malicious), /'=HYPERLINK/);

console.log('CSV roundtrip, subset export, filename safety, malformed/oversize input and formula safety passed.');
