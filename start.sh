#!/bin/sh
cd /opt/FeedExpander/
nohup java -Xmx256M -jar /opt/FeedExpander/target/expander-0.0.1.jar server config.yml > /var/log/FeedExpander.log &