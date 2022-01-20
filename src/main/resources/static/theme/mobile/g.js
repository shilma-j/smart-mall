// 页脚渲染
var footerNavItems = [
    {text: "主 页", img: jsPath + "/theme/" + themeName + "/footer/home.svg", href: "/", cls:""},
    {text: "分 类", img: jsPath + "/theme/" + themeName + "/footer/category.svg", href: "/category", cls:""},
    {text: "购物车", img: jsPath + "/theme/" + themeName + "/footer/cart.svg", href: "/cart", cls: ""},
    {text: "我 的", img: jsPath + "/theme/" + themeName + "/footer/user.svg", href: "/user/central", cls:""}
]
let tmp = location.pathname
if (tmp === "/") {
    footerNavItems[0].img = jsPath + "/theme/" + themeName + "/footer/home1.svg"
    footerNavItems[0].cls = "red"
} else if (tmp === "/category") {
    footerNavItems[1].img = jsPath + "/theme/" + themeName + "/footer/category1.svg"
    footerNavItems[1].cls = "red"
} else if (tmp.startsWith("/cart")) {
    footerNavItems[2].img = jsPath + "/theme/" + themeName + "/footer/cart1.svg"
    footerNavItems[2].cls = "red"
} else if (tmp.startsWith("/user")) {
    footerNavItems[3].img = jsPath + "/theme/" + themeName + "/footer/user1.svg"
    footerNavItems[3].cls = "red"
}
Vue.createApp({
    data() {
        return {
            items: footerNavItems
        }
    }
}).mount('#footerNav')



var $ = function (selectors) {
    return document.querySelector(selectors);
}

//functions
function htmlEncode(value) {
    return "";
}

function htmlDecode(value) {
    return "";
}

function priceFormat(num) {
    if (num < 10) {
        return "0.0" + num;
    } else if (num < 100) {
        return "0." + num;
    }
    let rem = num % 100;
    let i = Math.floor(num / 100);
    if (rem < 10) {
        return i + ".0" + rem;
    } else {
        return i + "." + rem;
    }
}

function subString(s, n) {
    return s.slice(0, n).replace(/([^x00-xff])/g, "$1a").slice(0, n).replace(/([^x00-xff])a/g, "$1");
}

function hideToast() {
    let toastBox = document.querySelector("#toastBox")
    if (toastBox !== null) {
        document.body.removeChild(toastBox)
    }
}

/**
 * @param msg     要显示的消息
 * @param timeout 隐藏延时(毫秒) 默认三秒, 0为不自动隐藏
 */
function showToast(msg, timeout = 3000) {
    hideToast()
    let toastBox = document.createElement("div")
    toastBox.setAttribute("id", "toastBox")
    toastBox.innerText = msg
    document.body.append(toastBox)
    if (timeout > 0) {
        setTimeout(evt => { hideToast() }, timeout)
    }
}

document.page = {
    captcha: document.getElementById("captcha"),
    currencySymbol: '￥',
    posting: false
};

if (document.page.captcha !== null) {
    document.page.captcha.onclick = function () {
        this.src = "/captcha?" + new Date().getTime();
    };
    document.page.captcha.click();
}

/**
 * delete request
 */
function deleteRequest(path, msg, callback) {
    if (msg != null && !confirm(msg)) {
        return;
    }
    if (typeof callback !== 'function') {
        callback = function () {
        };
    }
}

document.querySelectorAll("form").forEach(form => {
    // 有vue绑定的跳过
    if (form.getAttribute("@submit.prevent") !== null || form.getAttribute("v-on:submit.prevent") !== null) {
        return
    }

    var method = form.getAttribute("method");
    if (method === null) {
        method = "get";
    }
    var ajax = form.getAttribute("ajax");
    if (ajax === null) {
        ajax = "on";
    }
    if (method.toLowerCase() !== "post" || ajax.toLowerCase() !== "on") {
        return;
    }

    form.addEventListener("submit", evt => {
        evt.preventDefault();
        if (document.page.posting) {
            return;
        }
        document.page.posting = false;
        var formData = new FormData(form);
        fetch(form.action, {
            method: "post",
            body: formData,
            credentials: "same-origin"
        }).then(response => {
            if (response.status !== 200) {
                alert("服务器返回异常,请重试!\r\nresponse status code:" + response.status);
                return null;
            }
            return response.json()
        }).then(data => {
            if (data === null) {
                return;
            }
            if (typeof data.msg === "string" && data.msg.length > 0) {
                showToast(data.msg);
            }
            if (typeof data.url === "string" && data.url.length > 0) {
                document.location = data.url;
            }
            // refresh captcha
            if (typeof data.captcha === "boolean" && data.captcha) {
                var tmp = $("input[name=captcha]");
                if (tmp !== null) {
                    tmp.value = "";
                }
                if (document.page.captcha !== null) {
                    document.page.captcha.click();
                }
            }
            document.page.posting = false;
        }).catch(err => {
            alert("未知错误,请刷新重试\r\n" + err);
        })
    });
});

