/*
 * articleCatalog v2.0
 * Copyright(c) 2016 by bulandent
 * Date: 2017-5-27 16:10:41
 * Updated: 2020-10-10 17:40:04
**/
;
let articleCatalog = (function() {
    if ( !document.getElementById('postAr') || document.querySelectorAll('.headerlink').length === 0 || window.innerWidth < 900 ) {
        return function(){};
    }
    let DEFAULT = {
        lineHeight: 28,           // 每个菜单的行高是 28
        moreHeight: 10,           // 菜单左侧的线比菜单多出的高度
        surplusHeight: 180,       // 除了菜单高度+留白高度
        delay: 200,               // 防抖的延迟时间
        duration: 200,            // 滚动的动画持续时间
        toTopDistance: 80,        // 距离视口顶部多少高度之内时候触发高亮
        selector: '.headerlink',  // 文章内容中标题标签的 selector
    }
    return function(args) {
        DEFAULT = Object.assign(DEFAULT, args)

        let arContentAnchor = document.querySelectorAll(DEFAULT.selector),
            catalogLength = arContentAnchor.length,
            maxCatalogCount = 0,          // 视口内能容纳的最大目录个数
            viewPortHeight = 0,           // 当前视口的高度
            marginTop = 0,                // 菜单的初始滚动距离
            defaultDirec = 'bottom',      // 默认滚动方向
            lastSH = 0,                   // 获取页面初始滚动距离
            lastOnIndex = 0,              // 上次高亮的目录索引
            catalogBody = [],             // .arCatalog-body
            catalogDl = null,             // .arCatalog-body dl
            catalogDd = [],			      // .arCatalog-body dd
            initBodyTop = 0,              // 目录可视区域的 top
            initDlBottom = 0,             // 目录 dl 的 bottom
            firstDdTop = 0,               // 第一个 dd 的 top
            bodyMidBottom = 0,            // 目录可视区域的中间位置的 dd 的 bottom
            bodyBCR = null,	              // 目录可视区域的边界值
            hasStopSetHighlight = false;  // 在点击目录子项的时候直接高亮当前目录，而不通过 scroll 事件触发 setHighlight 函数

        initCatalog()

        window.addEventListener('scroll', function() {
            debounce(setHighlight, DEFAULT.delay)()
            debounce(resetStatus, DEFAULT.delay)()
        }, false)

        if (catalogLength > maxCatalogCount) {
            window.addEventListener('scroll', function() {
                debounce(scrollCatalog, DEFAULT.delay)()
            }, false)
        }

        window.addEventListener('resize', function(e) {
            debounce(initCatalog, DEFAULT.delay)()
        }, false)

        // 初始化
        function initCatalog() {
            let tempHeight = window.innerHeight

            if (viewPortHeight !== tempHeight) {
                viewPortHeight = tempHeight
                maxCatalogCount = Math.floor((viewPortHeight - DEFAULT.surplusHeight) / DEFAULT.lineHeight)

                generateCatalog()

                catalogLength = arContentAnchor.length
                lastSH = window.pageYOffset
                catalogBody = document.querySelector('.arCatalog-body')
                catalogDl = document.querySelector('.arCatalog dl')
                catalogDd = document.querySelectorAll('.arCatalog dd')
                bodyBCR = catalogBody.getBoundingClientRect()
                initBodyTop = bodyBCR.top
                initDlBottom = initDlBottom || catalogDl.getBoundingClientRect().bottom
                firstDdTop = firstDdTop || catalogDd[0].getBoundingClientRect().top,
                    bodyMidBottom = initBodyTop + Math.ceil((maxCatalogCount / 2 )) * DEFAULT.lineHeight

                // 给目录子项绑定事件
                catalogDd.forEach((curr, index) => {
                    curr.addEventListener('click', function(e) {
                        e.preventDefault()
                        hasStopSetHighlight = true
                        document.querySelector('.arCatalog .on').classList.remove('on')
                        catalogDd[index].classList.add('on')
                        lastOnIndex = index
                        let currTop = arContentAnchor[index].getBoundingClientRect().top
                        scrollToDest(currTop + window.pageYOffset - DEFAULT.toTopDistance)
                    }, false)
                });
            }
        }

        // 防抖：触发高频事件 n 秒后只会执行一次，如果 n 秒内事件再次触发，则会重新计时。
        function debounce(fn, delay = 200) {
            return function(args) {
                const _this = this
                clearTimeout(fn.id)
                fn.id = setTimeout(function() {
                    fn.apply(_this, args)
                }, delay)
            }
        }

        // 生成目录
        function generateCatalog(){
            let catalogHeight = arContentAnchor.length > maxCatalogCount ? maxCatalogCount * DEFAULT.lineHeight : arContentAnchor.length * DEFAULT.lineHeight;
            let retStr = `
						<div class="arCatalog">
							<div class="arCatalog-line" style="height: ${catalogHeight + DEFAULT.moreHeight}px"></div>
							<div class="arCatalog-body" style="max-height: ${catalogHeight}px; height: ${catalogHeight}px">
								<dl style="margin-top: ${marginTop}px">`
            let h2Index = 0,
                h3Index = 1,
                acIndex = '',
                tagName = '',
                index = 0;

            for (let currNode of arContentAnchor) {
                tagName = currNode.parentElement.tagName
                if ( tagName === 'H2') {
                    acIndex = ++h2Index
                    h3Index = 1
                    className = 'arCatalog-tack1'
                } else if (tagName === 'H3') {
                    acIndex = `${h2Index}.${h3Index++}`
                    className = 'arCatalog-tack2'
                } else {
                    acIndex = ''
                    className = 'arCatalog-tack3'
                }
                retStr += `
						<dd class="${className} ${index++ === lastOnIndex ? 'on' : ''}">
							<span class="arCatalog-index">${acIndex}</span>
							<a href="#">${currNode.title}</a>
							<span class="arCatalog-dot"></span>
						</dd>`
            };
            retStr += `</dl></div></div>`

            document.getElementById('arAnchorBar').innerHTML = retStr
        }

        // 自动滚动目录树，使得当前高亮目录在可视范围内
        function scrollCatalog() {
            let currentCatalog = document.querySelector('.arCatalog .on');

            let curr = currentCatalog.getBoundingClientRect(),
                list = catalogDl.getBoundingClientRect();

            if (defaultDirec === 'bottom') {  // 向下滚动
                if (curr.bottom + (maxCatalogCount / 2) * DEFAULT.lineHeight <= bodyBCR.bottom) {  // 上半部分
                    // 不滚动
                } else if (curr.bottom - bodyMidBottom < list.bottom - bodyBCR.bottom) {  // 中位以下
                    marginTop += -Math.floor((curr.bottom - bodyMidBottom ) / DEFAULT.lineHeight) * DEFAULT.lineHeight
                } else if (bodyBCR.bottom <= list.bottom) {  // 当剩余滚动距离
                    marginTop = bodyBCR.bottom - initDlBottom
                }
            } else {  // 向上滚动
                if (bodyBCR.top + (maxCatalogCount / 2) * DEFAULT.lineHeight <= curr.top) {
                    // 不滚动
                } else if (bodyMidBottom - curr.top < bodyBCR.top - list.top) {
                    marginTop += Math.floor((bodyMidBottom - curr.top) / DEFAULT.lineHeight) * DEFAULT.lineHeight
                } else if (list.top <= bodyBCR.top) {
                    marginTop = 0
                }
            }
            catalogDl.style.marginTop = marginTop + 'px'
        }

        // 动画实现滚动到目标位置
        function scrollToDest(destScrollTop) {
            let startTime;
            let currScrollTop = window.pageYOffset;
            function step(timestamp) {
                if (!startTime) {
                    startTime = timestamp
                }
                const elapsed = Math.round(timestamp - startTime)
                const distance = elapsed * ((Math.floor(destScrollTop) - currScrollTop) / DEFAULT.duration) + currScrollTop

                document.documentElement.scrollTop = document.body.scrollTop = distance

                if (elapsed < DEFAULT.duration) {
                    window.requestAnimationFrame(step)
                }
            }
            window.requestAnimationFrame(step)
        }

        // 高亮当前目录s
        function setHighlight(){
            defaultDirec = getScrollDirection()

            if (hasStopSetHighlight) {
                return
            }
            let {
                scrollTop,
            } = document.scrollingElement;

            let curr = document.querySelector('.arCatalog .on')

            let onIndex = [].indexOf.call(catalogDd, curr),  // 当前高亮索引
                nextOnIndex = onIndex;  // 滚动后高亮索引
            curr.classList.remove('on')

            let scrollHeight = document.documentElement.scrollHeight || document.body.scrollHeight
            if (arContentAnchor[catalogLength - 1].getBoundingClientRect().top <= DEFAULT.toTopDistance ||
                window.innerHeight + window.pageYOffset === scrollHeight) {  // 尾部
                lastOnIndex = catalogLength - 1
                catalogDd[lastOnIndex].classList.add('on')
            } else if (scrollTop <= firstDdTop) {  // 顶部
                catalogDd[0].classList.add('on')
                lastOnIndex = 0
            } else {  // 中间：使用缓存，直接从上一次索引（onIndex）位置开始查找
                if (defaultDirec === 'bottom') {
                    while (nextOnIndex < catalogLength) {
                        let currTop = arContentAnchor[nextOnIndex].getBoundingClientRect().top
                        if ( currTop > DEFAULT.toTopDistance && nextOnIndex > 0){
                            nextOnIndex--
                            break
                        }
                        nextOnIndex++
                    }
                } else {
                    while (nextOnIndex >= 0) {
                        let currTop = arContentAnchor[nextOnIndex].getBoundingClientRect().top
                        if ( currTop <= DEFAULT.toTopDistance){
                            break
                        }
                        nextOnIndex--
                    }
                }
                nextOnIndex = nextOnIndex === catalogLength ? nextOnIndex - 1 : nextOnIndex < 0 ? 0 : nextOnIndex
                lastOnIndex = nextOnIndex
                catalogDd[nextOnIndex].classList.add('on')
            }
        }

        // 获取最近一次页面的滚动方向
        function getScrollDirection() {
            let sh = window.pageYOffset, ret = 'bottom'
            if (sh < lastSH) {
                ret = 'top'
            }
            lastSH = sh
            return ret
        }

        function resetStatus() {
            if (hasStopSetHighlight) {
                hasStopSetHighlight = false
            }
        }
    }
}());