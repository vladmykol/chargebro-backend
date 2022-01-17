# ChargeBro backend

It is Spring boot based backed application for maneging charging stations and providing API for mobile app

## Run locally

1. Install [MongoDB](https://docs.mongodb.com/manual/administration/install-community/)
2. Add `application-default.properties` [here](./src/main/resources) with following properties:
> SMS_GATEWAY=localhost  
> PAY_USER=?  
> PAY_PASS=test  
> PAY_LINK=?  
> SMS_GATEWAY_TOKEN=?  
> TELEGRAM_BOT_KEY=?  
> TELEGRAM_ADMIN_CHAT_ID=?  
2. Run [TakeAndChargeApplication](./src/main/java/com/mykovolod/takeandcharge/TakeAndChargeApplication.java)

###Charging stations connection
We need to forward external requests from charging stations to our local machine IP
1. Set static IP for MacAddress with [static DHCP reservations](http://networkingforintegrators.com/2012/08/dhcp-reservations/)
2. Forward ports 10382 and 10381 to you local IP with [port forwarding](http://www.icafemenu.com/how-to-port-forward-in-mikrotik-router.htm)

###Maintenance
1. To check for project dependency vulnerabilities run:
   `./gradlew dependencyCheckAnalyze`
1. To update project dependencies automatically to latest versions run: `./gradlew useLatestVersions`

## Deploy with dokku in [Hetzner Clound](https://console.hetzner.cloud/)

### Setup new sever
1. Create [new server and access it via SSH](https://www.banjocode.com/post/hosting/setup-server-hetzner/)
1. Install [dokku](https://dokku.com/docs/getting-started/installation/#1-install-dokku) on you newly created server 
1. Run following command to setup new application on dokku
```
dokku git:allow-host github.com
dokku mongo:create chargebro-db
dokku apps:create chargebro
dokku mongo:link chargebro-db chargebro
dokku docker-options:add chargebro deploy "-p 10382:10382/tcp"
dokku resource:limit --memory 500m chargebro
dokku domains:add chargebro api.chargebro.com
dokku checks:disable chargebro
dokku config:set server DOKKU_LETSENCRYPT_EMAIL='info@chargebro.com'
   AUTH_TOKEN='???'
   CALLBACK_URI='https://api.chargebro.com' JAVA_OPTS='-Xmx200m'
   PAY_LINK='???'
   PAY_PASS='???' PAY_USER='???'
   SMS_GATEWAY='???'
   SMS_GATEWAY_TOKEN='???'
   SMS_TOKEN='???'
   TELEGRAM_BOT_KEY='???'
   TELEGRAM_ADMIN_CHAT_ID='???'
```
4. Create personal [access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token#creating-a-token) in GitHub account
4. Add that toke to dokku:
   `dokku git:auth github.com ?username? ?personal-access-token?`
4. Enable SSL certificate once application is deployed:
   `dokku letsencrypt:enable chargebro`

###Deploy steps
1. Push the latest changes to GitHub
1. Stop app to free up memory for a build, otherwise deploy will fail if you have less than 2GB RAM: `dokku ps:stop chargebro`
1. Login with SSH to your server and run
```
dokku git:sync --build chargebro https://github.com/mykovolod/chargebro-backend.git
```
4. Make sure you set up system firewall. Otherwise, to completely disable it by:
   `ufw disable`
4. Check last 1000 lines of logs:
   `dokku logs chargebro -n 1000`

### How to destroy env
```
dokku apps:destroy chargebro
cd /home/dokku
rm -rf chargebro
```

##Connect to DB from outside
###expose MongoDB
1. `dokku mongo:expose chargebro-db 27017 27018 27019 28017`
1. Disable server Firewall rules if any
1. See connection string `dokku config:show chargebro` and replace host by actual server IP

###un-expose MongoDB
1. `dokku mongo:unexpose chargebro-db`
1. Enable server firewall rules if any
