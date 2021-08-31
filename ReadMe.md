# ChargeBro server

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

## How to create new env
1. Login to DigitalOcean and create droplet
1. Run in console
```
dokku mongo:create take-and-charge-db
dokku apps:create server
dokku mongo:link take-and-charge-db server
dokku resource:limit --memory 270m server
dokku plugin:install https://github.com/dokku/dokku-letsencrypt.git 
dokku domains:add server api.chargebro.com
dokku config:set server DOKKU_LETSENCRYPT_EMAIL='info@chargebro.com'
   AUTH_TOKEN='???'
   CALLBACK_URI='https://api.chargebro.com' JAVA_OPTS='-Xmx170m
   -XX:MaxRAM=220m -XX:+UseSerialGC -XX:MaxRAMPercentage=30'
   PAY_LINK='???'
   PAY_PASS='???' PAY_USER='???'
   SMS_GATEWAY='???'
   SMS_GATEWAY_TOKEN='???'
   SMS_TOKEN='???'
   TELEGRAM_BOT_KEY='???'
   TELEGRAM_ADMIN_CHAT_ID='???'
dokku letsencrypt:enable server
dokku proxy:ports-remove server http:80:5000
```
3. `dokku docker-options:add server deploy "-p 10382:10382/tcp"`  - to open tcp port. Taken from [here](https://github.com/dokku/dokku/issues/2767)
4. `dokku docker-options:add bot build,deploy "--cpus='0.7'
   -m='200m'"` - to not use all resources during deploy

### How to destroy env
```
dokku apps:destroy server
cd /home/dokku
rm -rf server
```

###Other console commands
1. See memory usage in docker
   `docker stats`
1. Ubuntu memory by process. Press Shift + H to group threads and show only processes  
   `htop`
1. Free up some docker memory  
   `free -h && sudo sysctl -w vm.drop_caches=3 && sudo sync && echo 3 | sudo tee /proc/sys/vm/drop_caches && free -h`  
   `sync; echo 3 > /proc/sys/vm/drop_caches`
1. To see container restarts count  
   `docker inspect --format "ID: {{.ID}} RESTARTS: {{.RestartCount}} NAME: {{.Name}}"
   $(docker ps -aq)`


##Expose MongoDB
1. `dokku mongo:expose take-and-charge-db 27017 27018 27019 28017`
2. `ufw disable`
3. Remove droplet from Firewall rules in digitalOcean

###un-expose MongoDB
1. `dokku mongo:unexpose take-and-charge-db`
1. `ufw enable`
1. Add droplet to Firewall rules in digitalOcean


## Deploy with dokku in DigitalOcean

###initial setup
1. Create dokku repo  
   `dokku git:allow-host github.com`  
1. Create personal access token in GitHub account
1. Add that toke to dokku  
   `dokku git:auth github.com ?username? ?personal-access-token?`

###deploy steps
1. Push the latest changes to GitHub
1. Login to DigitalOcean and use console
1. Stop app to free up memory for a build, otherwise deploy will fail as container  
   `dokku ps:stop server`
1. `dokku git:sync --build server https://github.com/mykovolod/take-and-charge-server.git`
1. Check logs
   `dokku logs server -t`
