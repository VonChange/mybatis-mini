   Date.prototype.format = function (fmt) {
        var o = {
            "M+": this.getMonth() + 1, //月份
            "d+": this.getDate(), //日
            "h+": this.getHours() % 12 == 0 ? 12 : this.getHours() % 12, //小时
            "H+": this.getHours(), //小时
            "m+": this.getMinutes(), //分
            "s+": this.getSeconds(), //秒
            "q+": Math.floor((this.getMonth() + 3) / 3), //季度
            "S": this.getMilliseconds() //毫秒
        };
        var week = {
            "0": "天",
            "1": "一",
            "2": "二",
            "3": "三",
            "4": "四",
            "5": "五",
            "6": "六"
        };
        if (/(y+)/.test(fmt)) {
            fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
        }
        if (/(E+)/.test(fmt)) {
            fmt = fmt.replace(RegExp.$1, ((RegExp.$1.length > 1) ? (RegExp.$1.length > 2 ? "星期" : "周") : "") + week[this.getDay() + ""]);
        }
        for (var k in o) {
            if (new RegExp("(" + k + ")").test(fmt)) {
                fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
            }
        }
        return fmt;
    };
	  String.prototype.trim = function () {
        var str = this;
        return str.replace(/(^[\s\u3000]*)|([\s\u3000]*$)/g, "");
    };
    String.prototype.format = function (args) {
        var result = this;
        if (arguments.length > 0) {

            if (arguments.length == 1 && typeof (args) == "object" && null != args) {
                for (var key in args) {
                    if (args[key] != undefined) {
                        if (null == args[key]) {
                            args[key] = ""
                        }
                        ;
                        var reg = new RegExp("({" + key + "})", "g");
                        result = result.replace(reg, args[key]);
                    }
                }
            } else {
                for (var i = 0; i < arguments.length; i++) {
                    if (arguments[i] != undefined) {
                        if (null == arguments[i]) {
                            arguments[i] = ""
                        }
                        ;
                        //var reg = new RegExp("({[" + i + "]})", "g");//这个在索引大于9时会有问题，谢谢何以笙箫的指出
                        var reg = new RegExp("({)" + i + "(})", "g");
                        result = result.replace(reg, arguments[i]);
                    }
                }
            }
        }
        return result;
    };
	var DateAdd= function (interval, number, date) {
        /*
         *   功能:实现VBScript的DateAdd功能.
         *   参数:interval,字符串表达式，表示要添加的时间间隔.
         *   参数:number,数值表达式，表示要添加的时间间隔的个数.
         *   参数:date,时间对象.
         *   返回:新的时间对象.
         *   var   now   =   new   Date();
         *   var   newDate   =   DateAdd( "d ",5,now);
         *---------------   DateAdd(interval,number,date)   -----------------
         */
        switch (interval) {
            case   "y"   :
            {
                date.setFullYear(date.getFullYear() + number);
                return   date;
                break;
            }
            case   "q"   :
            {
                date.setMonth(date.getMonth() + number * 3);
                return   date;
                break;
            }
            case   "m"   :
            {
                date.setMonth(date.getMonth() + number);
                return   date;
                break;
            }
            case   "w"   :
            {
                date.setDate(date.getDate() + number * 7);
                return   date;
                break;
            }
            case   "d"   :
            {
                date.setDate(date.getDate() + number);
                return   date;
                break;
            }
            case   "h"   :
            {
                date.setHours(date.getHours() + number);
                return   date;
                break;
            }
            case   "m"   :
            {
                date.setMinutes(date.getMinutes() + number);
                return   date;
                break;
            }
            case   "s"   :
            {
                date.setSeconds(date.getSeconds() + number);
                return   date;
                break;
            }
            default   :
            {
                date.setDate(d.getDate() + number);
                return   date;
                break;
            }
        }
    };