package com.inktech.autoseal.model;

import java.io.Serializable;

/**
 * Created by Chaoyu on 2017/9/12.
 */

public class UsingSealInfoItem extends OutSealInfoItem implements Serializable {
    private Integer count;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
