package io.github.taybct.tool.core.poi.easypoi.enums;

/**
 * 字典转换类型
 */
public enum DictConvertType {
    VALUE_TO_NAME("值转名称", 1),
    NAME_TO_VALUE("名称转值", 2);

    private String desc;
    private Integer val;

    DictConvertType(String desc, Integer val) {
        this.desc = desc;
        this.val = val;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getVal() {
        return val;
    }

    public void setVal(Integer val) {
        this.val = val;
    }
}
