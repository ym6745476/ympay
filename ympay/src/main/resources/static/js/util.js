

/**
 * 获取URL参数
 * @param name 参数名称
 * @returns
 */
function getUrlParam(name) {
    //构造一个含有目标参数的正则表达式对象
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    //匹配目标参数
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return r[2];
    return null;
}

/**
 * 去除重复数组
 */
function repeat(array) {
    var r = [];
    for (var i = 0, l = array.length; i < l; i++) {
        for (var j = i + 1; j < l; j++)
            if (array[i] === array[j])
                j = ++i;
        r.push(array[i]);
    }
    return r;
}

/**
 * 数组是否含有指定元素
 */
Array.prototype.contains = function(item){
    return RegExp("\\b"+item+"\\b").test(this);
};

/**
 * 删除数组内指定值元素
 * array 数组
 * val值
 */
function arrayRemove(array, val) {
    var index = -1;
    for (var i = 0; i < array.length; i++) {
        if (array[i] == val) {
            index = i;
        }
    }
    if (index > -1) {
        array.splice(index, 1);
    }
}

/**
 * 比较两个数组是否相等
 */
function compare(a1, a2) {
    var exists = false;
    if (a1 instanceof Array && a2 instanceof Array) {
        for (var i = 0, iLen = a1.length; i < iLen; i++) {
            for (var j = 0, jLen = a2.length; j < jLen; j++) {
                if (a1[i] === a2[j]) {
                    return true;
                }
            }
        }
    }
    return exists;
}

/**
 * 判断字符串是数值
 */
function isNumeric(n) {
    var s = String(n)
    if (!/^[0-9.eE+-]+$/.test(s)) return false;
    var v = Number(s)
    return Number.isFinite(v)
}