package com.github.liueyueyi.forum.api.model.enums;

import com.github.liueyueyi.forum.api.model.vo.article.dto.FlagBitDTO;
import lombok.Getter;

/**
 * 操作文章
 *
 * @author louzai
 * @since 2022/7/19
 */
@Getter
public enum OperateArticleTypeEnum {

    OFFICAL(1, "官方") {
        @Override
        public FlagBitDTO getFlagBit() {
            return new FlagBitDTO(FlagBitEnum.OFFICAL.getCode(), Boolean.TRUE);
        }
    },
    CANCEL_OFFICAL(2, "取消官方"){
        @Override
        public FlagBitDTO getFlagBit() {
            return new FlagBitDTO(FlagBitEnum.OFFICAL.getCode(), Boolean.FALSE);
        }
    },
    TOPPING(3, "置顶"){
        @Override
        public FlagBitDTO getFlagBit() {
            return new FlagBitDTO(FlagBitEnum.TOPPING.getCode(), Boolean.TRUE);
        }
    },
    CANCEL_TOPPING(4, "取消置顶"){
        @Override
        public FlagBitDTO getFlagBit() {
            return new FlagBitDTO(FlagBitEnum.TOPPING.getCode(), Boolean.FALSE);
        }
    },
    CREAM(5, "加精"){
        @Override
        public FlagBitDTO getFlagBit() {
            return new FlagBitDTO(FlagBitEnum.CREAM.getCode(), Boolean.TRUE);
        }
    },
    CANCEL_CREAM(6, "取消加精"){
        @Override
        public FlagBitDTO getFlagBit() {
            return new FlagBitDTO(FlagBitEnum.CREAM.getCode(), Boolean.FALSE);
        }
    };

    OperateArticleTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final Integer code;
    private final String desc;

    public static OperateArticleTypeEnum formCode(Integer code) {
        for (OperateArticleTypeEnum value : OperateArticleTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return OperateArticleTypeEnum.OFFICAL;
    }

    public abstract FlagBitDTO getFlagBit();
}
