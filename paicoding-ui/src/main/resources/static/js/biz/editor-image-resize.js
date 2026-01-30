/**
 * Editor.md 图片拖拽缩放功能模块 - Moveable 版本
 * 
 * 功能：
 * 1. 在编辑器预览区为图片添加专业的拖拽缩放控制（8个控制点）
 * 2. 拖拽结束后自动将 Markdown 图片语法转换为 HTML img 标签
 * 3. 支持显示实时尺寸提示（宽×高）
 * 4. 保持宽高比缩放
 * 
 * 使用：
 * EditorImageResize.init(editorInstance);
 * 
 * @author PaiCoding
 * @date 2026-01-30
 * @version 2.0 (Moveable)
 */
(function() {
  'use strict';
  
  const EditorImageResize = {
    editor: null,
    previewContainer: null,
    currentMoveable: null,
    
    /**
     * 初始化图片缩放功能
     * @param {Object} editor Editor.md 实例
     */
    init: function(editor) {
      if (!editor) {
        console.error('[EditorImageResize] 编辑器实例不能为空');
        return;
      }
      
      this.editor = editor;
      this.previewContainer = editor.preview;
      
      // 转换为原生 DOM
      let domNode = this.previewContainer;
      if (this.previewContainer.jquery) {
        domNode = this.previewContainer[0] || this.previewContainer.get(0);
      }
      
      if (!domNode || !(domNode instanceof Node)) {
        console.error('[EditorImageResize] 预览容器未找到');
        return;
      }
      
      this.previewContainer = domNode;
      
      // 绑定点击事件
      this.bindClickEvent();
      
      // 点击其他地方取消选择
      this.bindDocumentClick();
      
      console.log('[EditorImageResize] 图片缩放功能已初始化（Moveable 版本）');
    },
    
    /**
     * 绑定图片点击事件
     */
    bindClickEvent: function() {
      const self = this;
      
      this.previewContainer.addEventListener('click', function(e) {
        if (e.target.tagName === 'IMG') {
          e.stopPropagation();
          self.enableImageResize(e.target);
        }
      });
    },
    
    /**
     * 绑定文档点击事件（取消选择）
     */
    bindDocumentClick: function() {
      const self = this;
      
      document.addEventListener('click', function(e) {
        // 点击非图片区域时取消选择
        if (e.target.tagName !== 'IMG' && !e.target.closest('.moveable-control')) {
          self.destroyCurrentMoveable();
        }
      });
    },
    
    /**
     * 启用图片缩放
     * @param {HTMLImageElement} img 图片元素
     */
    enableImageResize: function(img) {
      const self = this;
      
      // 检查 Moveable 是否加载
      if (typeof Moveable === 'undefined') {
        console.error('[EditorImageResize] Moveable 库未加载');
        alert('图片缩放功能加载失败，请刷新页面重试');
        return;
      }
      
      // 销毁之前的实例
      this.destroyCurrentMoveable();
      
      // 添加选中样式
      img.classList.add('image-selected');
      
      // 获取图片原始尺寸
      const originalWidth = img.naturalWidth || img.width;
      const originalHeight = img.naturalHeight || img.height;
      
      // 创建 Moveable 实例
      this.currentMoveable = new Moveable(document.body, {
        target: img,
        
        // 启用功能
        resizable: true,
        
        // 保持宽高比
        keepRatio: true,
        
        // 8个控制点：四边 + 四角
        renderDirections: ["nw", "n", "ne", "w", "e", "sw", "s", "se"],
        
        // 边缘检测
        edge: true,
        
        // 限制条件
        minWidth: 50,
        minHeight: 50,
        maxWidth: originalWidth * 2,
        maxHeight: originalHeight * 2,
        
        // 样式配置
        throttleResize: 0,
        zoom: 1,
      })
      .on("resize", function({ target, width, height, delta }) {
        // 实时更新图片尺寸
        target.style.width = width + 'px';
        target.style.height = height + 'px';
        target.style.maxWidth = '100%';
        
        // 显示尺寸提示
        self.showSizeTooltip(target, width, height);
      })
      .on("resizeEnd", function({ target }) {
        // 缩放结束，隐藏提示
        self.hideSizeTooltip();
        
        // 更新 Markdown 源码
        const finalWidth = parseInt(target.style.width);
        const finalHeight = parseInt(target.style.height);
        
        self.updateMarkdownSource(target, finalWidth, finalHeight);
      });
      
      // 监听滚动事件，更新 Moveable 位置
      const updateMoveablePosition = function() {
        if (self.currentMoveable) {
          self.currentMoveable.updateRect();
        }
      };
      
      // 绑定滚动事件
      this.previewContainer.addEventListener('scroll', updateMoveablePosition);
      window.addEventListener('scroll', updateMoveablePosition);
      
      // 存储事件处理器以便后续清理
      this.currentMoveable._scrollHandlers = {
        preview: updateMoveablePosition,
        window: updateMoveablePosition
      };
      
      console.log('[EditorImageResize] 已启用图片缩放:', img.src);
    },
    
    /**
     * 销毁当前 Moveable 实例
     */
    destroyCurrentMoveable: function() {
      if (this.currentMoveable) {
        // 移除选中样式
        const target = this.currentMoveable.target;
        if (target) {
          target.classList.remove('image-selected');
        }
        
        this.currentMoveable.destroy();
        this.currentMoveable = null;
      }
    },
    
    /**
     * 显示尺寸提示
     * @param {HTMLImageElement} img 图片元素
     * @param {Number} width 宽度
     * @param {Number} height 高度
     */
    showSizeTooltip: function(img, width, height) {
      let tooltip = document.getElementById('image-size-tooltip');
      
      if (!tooltip) {
        tooltip = document.createElement('div');
        tooltip.id = 'image-size-tooltip';
        tooltip.className = 'image-size-tooltip';
        document.body.appendChild(tooltip);
      }
      
      tooltip.textContent = Math.round(width) + ' × ' + Math.round(height);
      tooltip.style.display = 'block';
      
      // 定位到图片中心
      const rect = img.getBoundingClientRect();
      tooltip.style.left = (rect.left + rect.width / 2 - tooltip.offsetWidth / 2) + 'px';
      tooltip.style.top = (rect.top + rect.height / 2 - tooltip.offsetHeight / 2) + 'px';
    },
    
    /**
     * 隐藏尺寸提示
     */
    hideSizeTooltip: function() {
      const tooltip = document.getElementById('image-size-tooltip');
      if (tooltip) {
        tooltip.style.display = 'none';
      }
    },
    
    /**
     * 更新 Markdown 源码
     * @param {HTMLImageElement} img 图片元素
     * @param {Number} width 宽度
     * @param {Number} height 高度
     */
    updateMarkdownSource: function(img, width, height) {
      const cm = this.editor.cm;
      const imgSrc = img.getAttribute('src');
      const imgAlt = img.getAttribute('alt') || '';
      
      // 获取当前源码
      let markdown = cm.getValue();
      
      // 构造新的 HTML img 标签
      const newHtml = '<img src="' + imgSrc + '" alt="' + imgAlt + '" style="width: ' + width + 'px; max-width: 100%;">';
      
      // 尝试多种匹配模式
      let replaced = false;
      
      // 模式1: 匹配 Markdown 图片语法
      const mdPattern = new RegExp('!\\[' + this.escapeRegExp(imgAlt) + '\\]\\(' + this.escapeRegExp(imgSrc) + '\\)', 'g');
      if (markdown.match(mdPattern)) {
        markdown = markdown.replace(mdPattern, newHtml);
        replaced = true;
      }
      
      // 模式2: 匹配已有的 HTML img 标签
      if (!replaced) {
        const htmlPattern = new RegExp('<img[^>]*src=["\']' + this.escapeRegExp(imgSrc) + '["\'][^>]*>', 'gi');
        if (markdown.match(htmlPattern)) {
          markdown = markdown.replace(htmlPattern, newHtml);
          replaced = true;
        }
      }
      
      if (replaced) {
        // 保存光标位置
        const cursor = cm.getCursor();
        
        // 更新源码
        cm.setValue(markdown);
        
        // 恢复光标位置
        cm.setCursor(cursor);
        
        console.log('[EditorImageResize] 已更新源码，宽度:', width, '高度:', height);
      } else {
        console.warn('[EditorImageResize] 未找到匹配的图片标记');
      }
    },
    
    /**
     * 转义正则表达式特殊字符
     * @param {String} string 字符串
     * @returns {String} 转义后的字符串
     */
    escapeRegExp: function(string) {
      return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    }
  };
  
  // 暴露全局对象
  window.EditorImageResize = EditorImageResize;
  
  console.log('[EditorImageResize] 模块加载完成（Moveable 版本）');
  
})();
