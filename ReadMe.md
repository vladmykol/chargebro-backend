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
4. `dokku docker-options:add chargebro build,deploy "--cpus='0.7' -m='300m'"` - to not use all resources during deploy
4. Create personal [access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token#creating-a-token) in GitHub account
4. Add that toke to dokku  
   `dokku git:auth github.com ?username? ?personal-access-token?`
4. Enable SSL certificate once applicatio is deployed
   `dokku letsencrypt:enable chargebro`

###Deploy steps
1. Push the latest changes to GitHub
1. Login with SSH to your server and run
```
dokku git:sync --build server https://github.com/mykovolod/chargebro-backend.git
```
3. Check logs
   `dokku logs bot -t`

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
1. `dokku mongo:expose cragebro-db 27017 27018 27019 28017`
2. `ufw disable`
3. Remove droplet from Firewall rules in digitalOcean

###un-expose MongoDB
1. `dokku mongo:unexpose cragebro-db`
1. `ufw enable`
1. Add droplet to Firewall rules in digitalOcean
