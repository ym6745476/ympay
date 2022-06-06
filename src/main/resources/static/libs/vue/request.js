var token = '';
function get(that,url,params,successCallback,errorCallback){
    var userJson = window.sessionStorage.getItem('user');
    if(userJson){
        this.token = JSON.parse(userJson).token;
    }
            
    axios.get(url,{
        params:params,
        headers: {
            'Content-Type':'application/json',
            'token':this.token
        },
    }).then(
        function(response){
            console.log('success:', response.data);
            successCallback(response.data);
        }
    ).catch(
        function(error){
            console.log("error:",error);
            console.log("error:",error.response.data);
            errorCallback(error.response.data);
        }
    );
}


function post(that,url,params,successCallback,errorCallback){
    var userJson = window.sessionStorage.getItem('user');
    if(userJson){
        this.token = JSON.parse(userJson).token;
    }
    axios({
        url:url, 
        method:"post",
        headers: {
            'Content-Type':'application/x-www-form-urlencoded',
            'token':this.token
        },
        transformRequest: [function (data) {
		    var ret = '';
		    for (var it in data) {
			    ret += encodeURIComponent(it) + '=' + encodeURIComponent(data[it]) + '&';
		    }
		    return ret;
	    }],
        data:params
    }).then(
        function(response){
            console.log('success:', response.data);
            successCallback(response.data);
        }
    ).catch(
        function(error){
            console.log("error:",error.response.data);
            errorCallback(error.response.data);
        }
    );
}

function postJson(that,url,params,successCallback,errorCallback){
    var userJson = window.sessionStorage.getItem('user');
    if(userJson){
        this.token = JSON.parse(userJson).token;
    }
    axios({
        url:url, 
        method:"post",
        headers: {
            'Content-Type':'application/json',
            'token':this.token
        },
        transformRequest(data) {
		    return JSON.stringify(data);
	    },
        data:params
    }).then(
        function(response){
            console.log('success:', response.data);
            successCallback(response.data);
        }
    ).catch(
        function(error){
            console.log("error:",error.response.data);
            errorCallback(error.response.data);
        }
    );
}