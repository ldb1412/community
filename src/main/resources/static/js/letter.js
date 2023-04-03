$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");

	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();
	//异步请求
	$.post(
	    CONTEXT_PATH + "/letter/send",
	    {"toName":toName,"content":content},
	    function(data) {
	        data = $.parseJSON(data);//转化为JSON对象
	        if(data.code == 0) {//判断是否成功
	            $("#hintBody").text("发送成功!");
	        } else {
	            $("#hintBody").text(data.msg);
	        }

	        $("#hintModal").modal("show");
            setTimeout(function(){
                $("#hintModal").modal("hide");
                location.reload();//重新加载
            }, 2000);
	    }
	);
}

function delete_msg() {
	// TODO 删除数据
	var btn = this;
	var id = $(btn).prev().val();
	$.post(
		CONTEXT_PATH + "/letter/delete",
		{"id":id},
		function(data) {
			data = $.parseJSON(data);
			if(data.code == 0) {
				$(btn).parents(".media").remove();
			} else {
				alert(data.msg);
			}
		}
	);
}