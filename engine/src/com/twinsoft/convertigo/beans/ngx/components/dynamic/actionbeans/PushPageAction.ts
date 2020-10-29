    /**
     * Function PushPageAction
     *   
     * @param page  , the current page
     * @param props , the object which holds properties key-value pairs
     * @param vars  , the object which holds variables key-value pairs
     */
    PushPageAction(page: C8oPageBase, props, vars) : Promise<any> {
	
		let getPageSegment = function (pageName: string) {
			let appPages = page.routerProvider.pagesArray;
			for (let i=0; i < appPages.length; i++) {
				if (appPages[i].name == pageName) {
					return appPages[i].url
				}
			}
			return "/"
		}
		
        return new Promise((resolve, reject) => {
            /*let q:string = props.page; // qname of page
            let p:string = q.substring(q.lastIndexOf('.')+1);
            let version:string = props.tplVersion ? props.tplVersion : '';
            let greater: any = typeof page["compare"]!== "undefined" ? page["compare"]("7.7.0.2", version) : version.localeCompare("7.7.0.2");
            let v:any = greater ? p : page.getPageByName(p);
            page.routerProvider.push(v, props.data, { 
                animate: props.animate == "true" ? true:false,
                duration: props.animate_duration
            })
            .then((res:any) => {
                resolve(res)
            }).catch((error:any) => {
                reject(error)
            })*/
			
            let q:string = props.page; // qname of page
            let p:string = q.substring(q.lastIndexOf('.')+1);
			let path = getPageSegment(p);
			
			let url = ""
			let queryParams = props.data
			let segments = path.split("/")
			segments.forEach(function (segment) {
				if (segment.startsWith(":")) {
					let key = segment.substring(1)
					if (props.data[key]) {
						url = url + "/" + props.data[key]
						delete queryParams[key]
					}
				} else {
					url = url + "/" + segment
				}
			})
			
            let navController = page.getInstance(NavController)
            navController.navigateForward(url, { queryParams: queryParams })
            .then((res:any) => {
                resolve(res)
            }).catch((error:any) => {
                reject(error)
            })
            
        });
    }