/**
 * 
 */

 const toggleSidebar=()=>{
	if($(".sidebar").is(":visible")){
		$(".sidebar").css("display","none");
		$(".content").css("margin","0%");
	}
	else{
		$(".sidebar").css("display","block");
		$(".content").css("margin","20%");
	}
 };
 /*----- Search Contacts Request ------*/
 const search=()=>{
	 //console.log("Searching..");
	 let query = $("#search-input").val();
	 //console.log(query);
	 if(query == ''){
		 $(".search-result").hide();
	 }
	 else{
		 //sending request to server
		let url = `http://localhost:8080/search/${query}`;
		fetch(url).then((response)=>{
			return response.json();
		}).then((data)=>{
			//console.log(data);
			let text = `<div class = 'list-group'>`;
			data.forEach((contact)=>{
				text+=`<a  href = '/user/${contact.cid}/contact' class = 'list-group-item list-group-item-action'>${contact.name}</a>`
			});
			text+=`</div>`;
			//displaying fetched contacts
			$(".search-result").html(text);
		 	$(".search-result").show();
		}); 	 
		 
		 
	 }
 }
 
 /*----- Payment Gateway ------ */
 
 // First Request To Create order
 
 const paymentStart =()=>{
	 console.log("Payment Started");
	 let amount = $("#payment_field").val();
	 console.log(amount);
	 if(amount==""||amount == null){
		 alert("Empty Amount :(");
		 return;
	 }
	     const csrfToken = $("meta[name='_csrf']").attr("content");
	 //ajax to send request to server to create order
	 $.ajax(
		 {
			 url : "/user/create_order",
			 type : "POST",
			 data : JSON.stringify({amount:amount,info:'order_request'}),
			 contentType : "application/json",
			 dataType:"json",
			 headers: {
            	'X-CSRF-TOKEN': csrfToken
        	},
			 success:function(response){
				 //invoke when order id successfully created
				 console.log(response);
				 //below will be executed when we recieve successfully created of object in our response
				 if(response.status == 'created'){
					 //open payment form
					 let options = {
						 key:'rzp_test_sxKQjOHWhw2WeY',
						 amount:response.amount,
						 currency:response.currency,
						 name:'Smart Contact Manager Donation',
						 description:'Donation',
						 image:'https://img.freepik.com/free-vector/bird-colorful-logo-gradient-vector_343694-1365.jpg?w=740&t=st=1705261434~exp=1705262034~hmac=23eb6761b951cff28bf7cdaf9a79bcff76e5228a3c7b42d9efcd27dd703daeb0',
						 order_id: response.id,
						 handler:function(response){
							 console.log(response.razorpay_payment_id);
							 console.log(response.razorpay_order_id);
							 console.log(response.razorpay_signature);
							 console.log("Payment Successfull");
							 updatePaymentOnServer(response.razorpay_payment_id,response.razorpay_order_id,"Paid");
							 alert("Thank You For Donation :)")
						 },
						prefill: {
							name: "",
							email: "",
							contact: ""
						},
						notes: {
							address: "Testing App"
						},
						theme: {
							color: "#3399cc"
						}
					 };
					 let rzp = new Razorpay(options);
					 rzp.open();
					 rzp.on('payment.failed', function (response){
						console.log(response.error.code);
						console.log(response.error.description);
						console.log(response.error.source);
						console.log(response.error.step);
						console.log(response.error.reason);
						console.log(response.error.metadata.order_id);
						console.log(response.error.metadata.payment_id);
						alert("Payment Failed");
					});
				 }
			 },
			 error:function(error){
				 //invoke when error in creating order id
				 console.log(error)
				 alert("Something Went Wrong !!");
			 }
		 }
	 )
	
 }
 
 const updatePaymentOnServer= (paymentId,orderId,status)=>{
	 const csrfToken = $("meta[name='_csrf']").attr("content");
	 console.log(paymentId);
	 console.log(orderId);
	 console.log(status);
	 $.ajax({
			url : "/user/update_order",
			type : "POST",
			data : JSON.stringify({payment_id :paymentId,order_id : orderId,status:status}),
			contentType : "application/json",
			dataType:"json",
			headers: {
            	'X-CSRF-TOKEN': csrfToken
        	},
        	success:function(response){
				swal("Wohoo","Payment Successfull","success");
			},
			error:function(error){
				swal("Successfull","Your Payment is successfull,but not updated, will contact you","success");
			},
	 })
 }
 
 
 