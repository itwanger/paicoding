#!/bin/bash

# 脚本说明：用于向dict_common表添加新的字典数据，并自动更新Liquibase配置

# 检查是否提供了参数，如果没有则进入交互模式
if [ $# -lt 1 ]; then
    echo "=== 技术派项目字典数据添加工具 ==="
    echo "请输入以下信息以添加新的字典数据："
    
    # 读取type_code
    read -p "请输入字段名(type_code): " TYPE_CODE
    while [ -z "$TYPE_CODE" ]; do
        echo "字段名不能为空，请重新输入"
        read -p "请输入字段名(type_code): " TYPE_CODE
    done
    
    # 读取dict_code
    read -p "请输入字段值(dict_code): " DICT_CODE
    while [ -z "$DICT_CODE" ]; do
        echo "字段值不能为空，请重新输入"
        read -p "请输入字段值(dict_code): " DICT_CODE
    done
    
    # 读取dict_desc
    read -p "请输入描述(dict_desc): " DICT_DESC
    while [ -z "$DICT_DESC" ]; do
        echo "描述不能为空，请重新输入"
        read -p "请输入描述(dict_desc): " DICT_DESC
    done
    
    # 读取sort_no
    read -p "请输入排序号(sort_no): " SORT_NO
    while ! [[ "$SORT_NO" =~ ^[0-9]+$ ]]; do
        echo "排序号必须为数字，请重新输入"
        read -p "请输入排序号(sort_no): " SORT_NO
    done
    
    # 读取author，默认为沉默王二
    read -p "请输入作者(author，默认为沉默王二): " AUTHOR
    if [ -z "$AUTHOR" ]; then
        AUTHOR="沉默王二"
    fi
else
    # 检查参数
    if [ $# -lt 4 ]; then
        echo "使用方法: $0 <type_code> <dict_code> <dict_desc> <sort_no> [author]"
        echo "示例: $0 'ArticleStatus' '3' '已删除' 3 'YourName'"
        echo "或者直接运行 $0 进入交互模式"
        exit 1
    fi
    
    # 获取参数
    TYPE_CODE=$1
    DICT_CODE=$2
    DICT_DESC=$3
    SORT_NO=$4
    AUTHOR=${5:-"沉默王二"}
fi

# 获取当前日期
CURRENT_DATE=$(date +%Y%m%d)
CURRENT_DATETIME=$(date +"%Y-%m-%d %H:%M:%S")

# 定义文件路径
DATA_FILE="paicoding-web/src/main/resources/liquibase/data/init_data_${CURRENT_DATE}.sql"
CHANGELOG_FILE="paicoding-web/src/main/resources/liquibase/changelog/000_initial_schema.xml"

echo ""
echo "即将添加以下字典数据："
echo "  字段名(type_code): $TYPE_CODE"
echo "  字段值(dict_code): $DICT_CODE"
echo "  描述(dict_desc): $DICT_DESC"
echo "  排序号(sort_no): $SORT_NO"
echo "  作者(author): $AUTHOR"
echo ""

# 确认操作
read -p "确认添加以上数据？(y/N): " CONFIRM
if [[ ! "$CONFIRM" =~ ^[Yy]$ ]]; then
    echo "操作已取消"
    exit 0
fi

echo "开始添加字典数据到 $DATA_FILE"

# 检查数据文件是否已存在
if [ ! -f "$DATA_FILE" ]; then
    # 创建新的数据文件
    cat > "$DATA_FILE" << EOF
-- 数据字典添加
-- 创建时间: $CURRENT_DATETIME

EOF
    echo "已创建新文件: $DATA_FILE"
else
    echo "使用现有文件: $DATA_FILE"
fi

# 添加数据到SQL文件
echo "insert into dict_common(\`type_code\`,\`dict_code\`,\`dict_desc\`,\`sort_no\`) values('$TYPE_CODE','$DICT_CODE','$DICT_DESC',$SORT_NO);" >> "$DATA_FILE"
echo "已添加数据: type_code=$TYPE_CODE, dict_code=$DICT_CODE, dict_desc=$DICT_DESC, sort_no=$SORT_NO"

# 更新changelog文件
CHANGESET_ID="${CURRENT_DATE}_0"
LAST_LINE_NUM=$(grep -n "</databaseChangeLog>" "$CHANGELOG_FILE" | head -1 | cut -d: -f1)
INSERT_LINE_NUM=$((LAST_LINE_NUM - 1))

# 检查是否已经存在相同的变更集
EXISTING_CHANGESET=$(grep -c "init_data_${CURRENT_DATE}.sql" "$CHANGELOG_FILE")

if [ "$EXISTING_CHANGESET" -eq 0 ]; then
    # 在</databaseChangeLog>标签前插入新的变更集
    sed -i '' "${INSERT_LINE_NUM}i\\
    <!-- 自动添加字典数据 ${CURRENT_DATETIME} -->\\
    <changeSet id=\"$CHANGESET_ID\" author=\"$AUTHOR\">\\
        <sqlFile dbms=\"mysql\" endDelimiter=\";\" encoding=\"UTF-8\" path=\"liquibase/data/init_data_${CURRENT_DATE}.sql\"/>\\
    </changeSet>\\
" "$CHANGELOG_FILE"
    
    echo "已在 $CHANGELOG_FILE 中添加新的变更集"
else
    echo "变更集已存在于 $CHANGELOG_FILE 中，无需重复添加"
fi

echo ""
echo "操作完成！"
echo "1. 数据已添加到 $DATA_FILE"
echo "2. 变更集已更新到 $CHANGELOG_FILE"
echo "请检查文件内容并提交到版本控制系统"