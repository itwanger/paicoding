"""Editorial layout for the sprint plan in the archived Java interview guide."""
from hashlib import sha256
from html import escape


def first_month_note(cell):
    # Keep new revisions of the source out of this revision-specific rearrangement.
    value = cell['v']
    if sha256(value.encode()).hexdigest() != '0605fa89c4d690143b84c669710d711feaab3ad27851a48a26999b4ecdecb74e':
        return None

    links = [r['l'] for r in sorted(cell['r'], key=lambda r: r['t']) if r.get('l')]
    if len(links) != 11 or [value.index(url) for url in links] != sorted(value.index(url) for url in links):
        raise ValueError('请核对第一个月备注中教程与简历资料的链接')

    def link(index, label):
        return '<a href="' + escape(links[index], quote=True) + '">' + label + '</a>'

    def section(title, paragraphs):
        return '<p><strong>' + title + '</strong></p>' + ''.join('<p>' + p + '</p>' for p in paragraphs)

    intro, resources = value.split('技术派、派聪明、PmHub、Java 面试指南、', 1)
    advice = resources[resources.index('不管是日常实习'):]
    counts, choices = advice.split('项目的选择逻辑，')
    combos, outlook = choices.split('每一种组合')
    choices = combos.replace('如果你是 92', '如果你是 985、211 的同学').replace('双非本民办本学院本', '双非本科、民办本科和学院的同学')
    points = [s.strip('，；。 ') for s in choices.replace('，如果不想做轮子', '；如果不想做轮子').split('；')]
    return ''.join((
        section('第一个月怎么学', [escape(p.strip()) for p in intro.splitlines() if p.strip()]),
        section('教程与刷题资料', [
            link(0, '教程与源码获取方式') + '（技术派、派聪明、PmHub、Java 面试指南、二哥的 LeetCode 刷题笔记）。',
            '想早点上手 Vibe Coding，可以跟着' + link(1, 'PaiAgent 教程') + '做。学派聪明可以先看' + link(3, '派聪明学习路线') + '。',
            link(8, '面渣逆袭在线版') + '可以按专题复习。PDF 在星球的「文件」菜单里，也可以从' +
            link(10, '星球置顶帖') + '里的百度网盘、阿里云盘或夸克网盘下载。',
        ]),
        section('项目怎么写进简历', [
            'AI 项目：' + '、'.join([link(2, '派聪明简历写法'), link(4, 'PaiFlow 简历写法')]) + '。',
            '后端项目：' + '、'.join([link(5, '技术派简历写法'), link(6, 'MYDB 在线教程与简历写法'),
                                  link(7, 'PmHub 简历写法'), link(9, '点评、外卖、商城项目的简历写法')]) + '。',
        ]),
        section('项目怎么选', [escape(counts)]),
        '<ul>' + ''.join('<li>' + escape(p) + '。</li>' for p in points if p) + '</ul>',
        '<p>' + escape(('每一种组合' + outlook).replace('球友原则', '球友选择')) + '</p>',
    ))
