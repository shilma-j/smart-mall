const orderNo = document.querySelector("input[name=orderNo]").value;
const confirmBtn = document.querySelector("#confirmBtn");
const payBtn = document.querySelector("#payBtn");
const shipBtn = document.querySelector("#shipBtn");
const cancelBtn = document.querySelector("#cancelBtn");
const refundBtn = document.querySelector("#refundBtn");

// 点击支付按钮
if (payBtn !== null) {
    payBtn.addEventListener("click", evt => {
        if (window.confirm("确定要支付该订单么?")) {
            let data = new FormData();
            data.append("orderNo", orderNo);
            fetch('/admin/order/order/pay', {
                method: "POST",
                body: data,
                credentials: "same-origin"
            }).then(response => {
                if (response.status !== 200) {
                    alert("未知错误，请联系统管理员, status:" + response.status);
                    return;
                }
                response.json().then(json => {
                    if (json.msg !== null) {
                        alert(json.msg);
                    }
                    location.reload();
                });
            }).catch(error => {
                alert("网络错误");
            });
        }
    });
}

// 点击发货|修改物流信息按钮
if (shipBtn !== null) {
    shipBtn.addEventListener("click", evt => {

        let data = new FormData();
        let expressNo = document.querySelector("input[name=expressNo]").value.trim();
        console.log(expressNo);
        if (expressNo.length < 5) {
            alert("请输入正确的快递单号");
            return;
        }
        data.append("orderNo", orderNo);
        data.append("expressId", document.querySelector("select[name=expressId]").value);
        data.append("expressNo", expressNo);
        fetch('/admin/order/order/ship', {
            method: "POST",
            body: data,
            credentials: "same-origin"
        }).then(response => {
            if (response.status !== 200) {
                alert("未知错误，请联系统管理员, status:" + response.status);
                return;
            }
            response.json().then(json => {
                if (json.msg !== null) {
                    alert(json.msg);
                }
                location.reload();
            });
        }).catch(error => {
            alert("网络错误");
        });

    });
}

// 点击完成按钮
if (confirmBtn !== null) {
    confirmBtn.addEventListener("click", evt => {
        if (window.confirm("确定要完成该订单么?")) {
            let data = new FormData();
            data.append("orderNo", orderNo);
            fetch('/admin/order/order/confirm', {
                method: "POST",
                body: data,
                credentials: "same-origin"
            }).then(response => {
                if (response.status !== 200) {
                    alert("未知错误，请联系统管理员, status:" + response.status);
                    return;
                }
                response.json().then(json => {
                    if (json.msg !== null) {
                        alert(json.msg);
                    }
                    location.reload();
                });
            }).catch(error => {
                alert("网络错误");
            });
        }
    });
}

// 点击取消按钮
if (cancelBtn !== null) {
    cancelBtn.addEventListener("click", evt => {
        if (window.confirm("确定要取消该订单么?")) {
            let data = new FormData();
            data.append("orderNo", orderNo);
            fetch('/admin/order/order/cancel', {
                method: "POST",
                body: data,
                credentials: "same-origin"
            }).then(response => {
                if (response.status !== 200) {
                    alert("未知错误，请联系统管理员, status:" + response.status);
                    return;
                }
                response.json().then(json => {
                    if (json.msg !== null) {
                        alert(json.msg);
                    }
                    location.reload();
                });
            }).catch(error => {
                alert("网络错误");
            });
        }
    });
}

// 点击退款按钮
if (refundBtn !== null) {
    refundBtn.addEventListener("click", evt => {
        if (window.confirm("确定要退款该订单么?")) {
            let data = new FormData();
            data.append("orderNo", orderNo);
            fetch('/admin/order/order/refund', {
                method: "POST",
                body: data,
                credentials: "same-origin"
            }).then(response => {
                if (response.status !== 200) {
                    alert("未知错误，请联系统管理员, status:" + response.status);
                    return;
                }
                response.json().then(json => {
                    if (json.msg !== null) {
                        alert(json.msg);
                    }
                    location.reload();
                });
            }).catch(error => {
                alert("网络错误");
            });
        }
    });
}