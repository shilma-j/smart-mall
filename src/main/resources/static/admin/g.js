// 移动端判别参数
const MOBILE_AGENTS = [" (iPhone; CPU ", " (iPad; CPU ", " Android "]

// createElementFromHTML
function ele(htmlString) {
    let html = new DOMParser().parseFromString(htmlString, 'text/html');
    return html.body.firstChild;
}

//functions
function htmlEncode(str) {
    return document.createElement('a').appendChild(
        document.createTextNode(str)).parentNode.innerHTML;
}

function htmlDecode(value) {
    return "";
}

/**
 * 是否移动端
 * @returns {boolean}
 */
function isMobile() {
    var userAgent = navigator.userAgent;
    for (var i = 0; i < MOBILE_AGENTS.length; i++) {
        if (userAgent.indexOf(MOBILE_AGENTS[i]) > 0) {
            return true
        }
    }
    return false
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

document.page = {
    captcha: document.getElementById("captcha"),
    currencySymbol: '￥',
    posting: false
};

if (document.page.captcha !== null) {
    document.page.captcha.style.cursor = "pointer";
    document.page.captcha.onclick = function () {
        this.src = "/captcha?" + new Date().getTime();
    };
    document.page.captcha.click();
}

/**
 * delete request
 */
function deleteRequest(path, msg, callback) {
    if (msg == null) {
        msg = "确定要删除么？";
    }
    if (typeof callback !== 'function') {
        callback = function () {
        };
    }
    if (confirm(msg)) {
        fetch(path, {
            "method": "post"
        }).then(function (response) {
            if (response.status !== 200) {
                alert("服务器返回异常,请重试!\r\nresponse status code:" + response.status);
                return null;
            }
            return response.json()
        }).then(function (data) {
            if (data === null) {
                return;
            }
            if (typeof data.msg === "string" && data.msg.length > 0) {
                alert(data.msg);
            }
            if (typeof data.url === "string" && data.url.length > 0) {
                document.location = data.url;
            }
            document.page.posting = false;
        }).catch(err => {
            alert("未知错误,请刷新重试\r\n" + err);
        })
    }
}


document.querySelectorAll("form").forEach(function (form) {

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
    form.addEventListener("submit", function (ev) {

        ev.preventDefault();
        if (document.page.posting) {
            return;
        }
        document.page.posting = false;
        var formData = new FormData(form);
        fetch(form.action, {
            method: "post",
            body: formData,
            credentials: "same-origin"
        }).then(function (response) {
            if (response.status !== 200) {
                alert("服务器返回异常,请重试!\r\nresponse status code:" + response.status);
                return null;
            }
            return response.json()
        }).then(function (data) {
            if (data === null) {
                return;
            }
            if (typeof data.msg === "string" && data.msg.length > 0) {
                alert(data.msg);
            }
            if (typeof data.url === "string" && data.url.length > 0) {
                document.location = data.url;
            }
            // refresh captcha
            if (typeof data.captcha === "boolean" && data.captcha) {
                var tmp = document.querySelector("input[name=captcha]");
                if (tmp !== null) {
                    tmp.value = "";
                }
                if (document.page.captcha !== null) {
                    document.page.captcha.click();
                }
            }
            form.querySelectorAll(".inputError").forEach(function (ele) {
                ele.classList.remove("inputError");
            });
            var fe = form.querySelector("#formError");
            if (fe !== null) {
                fe.innerText = "";
                fe.style.display = "none";
            }

            if (typeof data.error == "object") {
                var i = 0;
                for (var key in data.error) {
                    if (i === 0 && fe !== null && data.error[key] !== null) {
                        fe.innerText = data.error[key];
                        fe.style.display = "block";
                    }
                    var ele = form.querySelector("input[name=" + key + "]");
                    if (typeof ele == "object") {
                        ele.classList.add("inputError");
                    }
                    i++;
                }
            }
            document.page.posting = false;
        }).catch(err => {
            alert("未知错误,请刷新重试\r\n" + err);
        })
    });
})

// 电脑端浏览器鼠标移动到餐单直接弹出，无需点击
if (!isMobile()) {
    document.querySelectorAll("nav.navbar ul li.nav-item.dropdown").forEach(item => {
        item.addEventListener("mouseenter", evt => {
            let ul = item.querySelector("ul.dropdown-menu")
            if (ul != null) {
                ul.classList.add("show")
            }
        })
        item.addEventListener("mouseleave", evt => {
            let ul = item.querySelector("ul.dropdown-menu")
            if (ul != null) {
                ul.classList.remove("show")
            }
        })
    })
}
