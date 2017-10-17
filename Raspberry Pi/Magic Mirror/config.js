var config = {
	port: 8080,
	ipWhitelist: [], 
	zoom: 0.8,
	language: "en",
	timeFormat: 24,
	units: "metric",

	modules: [
		{
			module: "alert",
		},
		{
			module: "updatenotification",
			position: "top_bar"
		},
		{
			module: "clock",
			position: "top_left"
		},
		{
			module: 'MMM-SystemStats',
			position: 'top_left', // This can be any of the regions.
			classes: 'small dimmed', // Add your own styling. Optional.
			config: {
				updateInterval: 10000,
				animationSpeed: 0,
				align: 'left', // align labels
				header: 'System Stats', // This is optional
			},
		},
		{
			module: "calendar",
			header: "Calendar",
			position: "top_left",
			
			config: {
				colored: true,
				calendars: [
					{
						symbol: "calendar-check-o",
						url: "URL_HERE",
						color: "#5555ff",
						maximumNumberOfDays: 30
					},
					{
						sybmol: "calendar-o",
						url: "URL_HERE",
						color: "#ff5555",
						maximumNumberOfDays: 30
					}
				]
			}
		},
		{
			module: "calendar",
			header: "TV Shows",
			position: "top_left",
			config: {
				calendars: [
					{
						symbol: "tv",
						url: "URL_HERE",
						maximumNumberOfDays: 10
					}
				]
			}
		},
		{
			module: "currentweather",
			position: "top_right",
			config: {
				location: "Zagreb,Croatia",
				locationID: "",  //ID from http://www.openweathermap.org/help/city_list.txt
				appid: "ID_HERE"
			}
		},
		{
			module: "weatherforecast",
			position: "top_right",
			header: "Weather Forecast",
			config: {
				location: "Zagreb,Croatia",
				locationID: "",  //ID from http://www.openweathermap.org/help/city_list.txt
				appid: "ID_HERE"
			}
		},
		{
			module: "newsfeed",
			position: "bottom_bar",
			config: {
				feeds: [
					{
						title: "HRT News",
						url: "URL_GERE"
					}
				],
				showSourceTitle: true,
				showPublishDate: true
			}
		},			
		{
			module: 'MMM-Remote-Control'
			// uncomment the following line to show the URL of the remote control on the mirror
			// , position: 'bottom_left'
			// you can hide this module afterwards from the remote control itself
		},		
		
		{
			module: "MMM-RTSPStream",
			position: "bottom_right",
			config: {
				autoStart: true,
				rotateStreams: false,
				rotateStreamTimeout: 10,
				moduleWidth: 274,
				moduleHeight: 230,
				localPlayer: 'omxplayer',
				remotePlayer: 'none',
				showSnapWhenPaused: false,
				remoteSnaps: false,
				moduleOffset: {
					left:-280,
					top:-475,
				},
				stream1: {
					name: 'Front Camera',
					url: 'URL_HERE',
					frameRate: 'undefined',
					width: 275,
					height: 225,
					},
				
				},
				
		},
		
		{
			module: "MMM-json-feed",
			position: "bottom_right",
			config: {
			  url: "URL_HERE",
			  title: "IoT Status",
			  updateInterval: 1000,
			  values: ["gate2","door_main","door_dominik","light_dominik"]
			}
		},
		{
			module: "MMM-GoogleAssistant",
			position: "top_center",
			config: {
				maxWidth: "100%",    
				header: ""
			}
		},		
		{
			module: 'MMM-LocalTransport',
			header: 'Bus to Velika Gorica',
			position: 'bottom_left',
			config: {
				api_key: 'API_KEY_HERE',
				origin: 'Turopoljska ul. 341c, 10419, Rakitovec',
				destination: '10410, Velika Gorica',
				maximumEntries: 1,
				maxWalkTime: 15,
				displayWalkType: 'none',
				maxModuleWidth: 250
			},
		},	
		{
			module: 'MMM-MyCommute',
			position: 'bottom_left',
			header: 'Commute Time' ,
			config: {
				apikey: 'API_KEY_HERE',
				origin: 'Turopoljska ul. 341c, 10419, Rakitovec',
				startTime: '00:00',
				endTime: '23:59',
				hideDays: [],
				destinations: [
				  {
					destination: 'Unska ul. 3, 10000, Zagreb',
					label: 'FER',
					mode: 'driving',
					color: '#4988ed'
				  },
				  {
					destination: 'Krapinska ul. 45, 10000, Zagreb',
					label: 'Ericcson Nikola Tesla',
					mode: 'driving',
					color: '#49ed61'
				  },
				  {
					destination: 'Ul. Bruna Bušića 7, 10408, Velika Mlaka',
					label: 'OŠ Velika Mlaka',
					mode: 'driving',
					color: '#ed4949'
				  },
				  {
					destination: 'Ul. kralja Stjepana Tomaševića 21, 10410, Velika Gorica',
					label: 'Gimnazija VG',
					mode: 'transit',
					color: '#edd749'
				  },
				  {
					destination: 'Ul. Grada Vukovara 269B, 10000, Zagreb',
					label: 'SŠ Vladimir Prelog',
					mode: 'transit',
					color: '#8749ed'
				  },
				  
				]
			  }
		},		
				
		
		
			
		
	]
		
};

/*************** DO NOT EDIT THE LINE BELOW ***************/
if (typeof module !== "undefined") {module.exports = config;}
