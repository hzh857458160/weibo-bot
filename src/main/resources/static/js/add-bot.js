var addBotKey = "";

function postTest1() {
    console.log("postTest1()")
    var warnTip = $("#warnTip");
    var failTip = $("#failTip");
    var successTip = $("#successTip");
    var submitBtn = $(".btn.btn-primary");

    //输入框检查
    if (!inputCheck()) {
        warnTip.text("(用户名或密码不能为空！)");
        warnTip.css('display', '');
        return;
    }

    //禁用按钮，并提示等待
    submitBtn.attr("disabled", true);
    warnTip.text("正在验证账号，请稍等...");
    warnTip.css('display', '');

    $.post(
        "/weibo-bot/test1",
        {
            username: $("#input-4").val().trim(),
            password: $("#input-5").val().trim()
        },
        function (data) {
            console.log(data);
            setTimeout(function () {
                warnTip.text("");
                warnTip.css('display', 'none');
                //根据结果处理成功失败提示提示
                if (data.code === "0") {
                    successTip.text("账号验证成功，开始为账号生成身份信息...");
                    successTip.css('display', '');
                    addBotKey = data.attach.key;
                    postTest2();


                } else {
                    if (data.code === "101") {
                        failTip.text("添加账号失败，账号密码有误，请确认后重试");
                    } else {
                        failTip.text("添加账号失败，请稍后重试");
                    }
                    showAlert($(".alert.alert-danger"));
                    //取消显示警告提示，恢复按钮
                    submitBtn.attr("disabled", false);
                }

            }, 2000);
        },
        "json"
    );
}

function postTest2() {
    console.log("postTest2()")
    var warnTip = $("#warnTip");
    var failTip = $("#failTip");
    var successTip = $("#successTip");
    var submitBtn = $(".btn.btn-primary");

    // successTip.text("");
    // successTip.css('display', 'none');
    // warnTip.css('display', '');
    // warnTip.text("(正在生成社交机器人信息，请稍等...)");

    $.post(
        "/weibo-bot/test2",
        {
            "key": addBotKey
        },
        function (data) {
            console.log(data);
            setTimeout(function () {
                //根据结果处理成功失败提示提示
                if (data.code === "0") {
                    var botInfo = data.attach.botInfo;
                    console.log(botInfo);
                    var gender = botInfo.gender == 0 ? "男" : "女";
                    var text = "生成身份信息成功，接下来将到微博中初始化账号...\n社交机器人信息( "
                        + "初始昵称：" + botInfo.nickName
                        + " 性别：" + gender
                        + " 出生日期：" + botInfo.birthDate
                        + " 兴趣爱好：" + botInfo.interests
                        + " 机器人等级：" + botInfo.botLevel
                        + ")";
                    successTip.text("");
                    successTip.text(text);
                    postTest3();

                } else {
                    if (data.code === "101") {
                        failTip.text("操作有误，您不在当前操作队列中");
                    } else {
                        failTip.text("添加账号失败，请稍后重试");
                    }
                    showAlert(failTip);
                    //取消显示警告提示，恢复按钮
                    // warnTip.text("");
                    // warnTip.css('display', 'none');
                    submitBtn.attr("disabled", false);
                }
            }, 2000);
        },
        "json"
    );
}

function postTest3() {
    console.log("postTest3()")
    var warnTip = $("#warnTip");
    var failTip = $("#failTip");
    var successTip = $("#successTip");
    var submitBtn = $(".btn.btn-primary");

    // //flag作为最后成功失败的标志
    // warnTip.css('display', '');
    // warnTip.text("(正在微博中设置机器人信息，请稍等...)");

    $.post(
        "/weibo-bot/test3",
        {
            "key": addBotKey
        },
        function (data) {
            console.log(data);
            setTimeout(function () {
                //取消显示警告提示，恢复按钮
                // warnTip.text("");
                // warnTip.css('display', 'none');
                submitBtn.attr("disabled", false);
                //根据结果处理成功失败提示提示
                if (data.code === "0") {
                    successTip.text("添加账号成功!");
                    var screen = data.attach.screen;
                    var botId = data.attach.botId;
                    var text1 = "<a href=\"/static/img/screenshots/" + screen + "\" target=\"_blank\" >查看微博信息设置截图</a>";
                    var text2 = "<a href=\"/weibo-bot/account?botId=" + botId + "\" target=\"_blank\" >查看新社交机器人信息</a>";
                    successTip.append(text1).append(" ").append(text2);
                    showAlert(successTip);
                    console.log(botInfo);

                } else if (data.code === "101") {
                    failTip.text("操作有误，您不在当前操作队列中");
                    showAlert(failTip);

                } else {
                    failTip.text("添加账号失败，请稍后重试");
                    showAlert(failTip);
                }

            }, 2000);
        },
        "json"
    );


}


function postAccount() {
    var warnTip = $("#warnTip");
    var failTip = $("#failTip");
    var successTip = $("#successTip");
    var submitBtn = $(".btn.btn-primary");

    //输入框检查
    if (!inputCheck()) {
        warnTip.text("(用户名或密码不能为空！)");
        warnTip.css('display', '');
        return;
    }
    //禁用按钮，并提示等待
    submitBtn.attr("disabled", true);
    warnTip.text("(正在添加账号，请稍等，这可能需要一段时间...)");
    warnTip.css('display', '');

    //flag作为最后成功失败的标志
    var code = "12";
    var attach = "";
    try {
        $.post(
            "/weibo-bot/account",
            {
                username: $("#input-4").val().trim(),
                password: $("#input-5").val().trim()
            },
            function (data) {
                console.log(data);
                code = data.code;
                attach = data.attach;
            },
            "json"
        );
    } finally {
        setTimeout(function () {
            //取消显示警告提示，恢复按钮
            warnTip.text("");
            warnTip.css('display', 'none');
            submitBtn.attr("disabled", false);
            //根据结果处理成功失败提示提示
            if (code === "0") {
                successTip.text("添加账号成功！");
                var text = "<a href=\"/weibo-bot/account?botId=" + attach + "\" target=\"_blank\" >点击此处查看新社交机器人信息</a>";
                successTip.append(text);
                showAlert($(".alert.alert-success"));
                $("#input-4").val("");
                $("#input-5").val("");

            } else if (code === "10") {
                failTip.text("添加账号失败，账号密码有误，请确认后重试");
                showAlert($(".alert.alert-danger"));

            } else {
                failTip.text("添加账号失败，请稍后重试");
                showAlert($(".alert.alert-danger"));
            }
        }, 2000);

    }


}

function showAlert(alert) {
    alert.css('display', '');
    setTimeout(function () {
        alert.css('display', 'none');
    }, 8000);
}

function inputCheck() {
    var username = $("#input-4").val().trim();
    var password = $("#input-5").val().trim();
    if (username === "" || password === "") {
        return false;
    }
    return true;
}