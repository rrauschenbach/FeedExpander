# FeedExpander
Feed expander is a service which provides a generic way to create rss or atom feeds containing a specific part of the linked page. In other words, with FeedExpander you can convert a cropped feed into a full feed.  

## Build
You need to have git, maven and java 1.8 installed before doing the following.
  * `git clone https://github.com/meerkatzenwildschein/FeedExpander.git` 
  * `cd FeedExpander/` 
  * `mvn install` 

## Install
 * Take the expander-x.x.x.jar, config.yml, whitelist.txt and htusers files and copy them all together where you want to run the feed expander. `cp config.yml htusers whitelist.txt target/expander-x.x.x.jar /tmp/`
 * Edit the config.yml and change the htuser file if you like. 
 * Start the service with `java -jar expander-x.x.x.jar server config.yml`

## Security hints
  The client have the possibility to make the FeedExpander service to load html from some self defined url. This can for example be misused to run DDOS attacks or to load some unexpected things over this service. It`s highly recommended to use the white list functionality to prohibit those cases.
  
  If you plan to use the FeedExpander service as some private service you have to configure the htusers files where you can simply add some users and their password which are allowed to get access. It would also be much more safe to use https instead of the preconfigured http setup.
  
  In the case that FeedExpander is running on the same machine as the feed reader you would use (for example TTRSS) it would be a good idea configure FeedExpander with `bindHost: 127.0.0.1` which is already the default configuration. 
It is generally a good idea to reduce the visibility of a service using a firewall or defining some iptable rules.  
  
## Usage
  After starting the FeedExpander service, you can connect to it as configured with the config.yml. The
  default configuration uses http://localhost:9998/expand as base url. You have to add the url parameters `feedUrl` 
  containing the url which points to the url of the feed to be expanded. You also need the `includes` url parameter
  which describes the part of the page, linked in the feed entries / articles.
  
  The `includes` url parameter expression, which selects a part of a html page describes a navigation path through the
  html tags. Each path element is separated by a slash and starts with the kind of element followed by a equality sign 
  followed by the search value.
  
  * `id=someId` selects the tag with the id `someId` somewhere in the page.
  * `tag=div 3` selects the third div came up from the previous element or the body element if no previous one was defined.
  * `tag=*article` selects the first article tag somewhere in the page.
  
  To include multiple page parts, additional expressions can be added by separating them with a `|` character. The following example expression select the first three div child tags of the parent tag with the id `someId`.
  
  * `id=someId/tag=div|id=someId/tag=div 2|id=someId/tag=div 3`
  
## Examples
  Please note that the parameter values must be url encoded. You can use [url-encode-decode.com](http://www.url-encode-decode.com) for example. The following url configurations are randomly picked and only have to be understand as an example. There exists NO agreement with page proprietor which allows to expand their feeds for commercial or private reasons. 

  * golem.de feed at rss.golem.de/rss.php?feed=ATOM1.0 can be expanded by including the screen element, than the third div and the article element below `id=screen/tag=div 3/tag=article`.
    `http://localhost:9998/expand?feedUrl=http%3A%2F%2Frss.golem.de%2Frss.php%3Ffeed%3DATOM1.0&include=id%3Dscreen%2Ftag%3Ddiv+3%2Ftag%3Darticle`
  It is also possible to select the `article` element directly with simply `tag=*article` which selects all articles where the first one is automatically used. If there`re more than
  one, the right one can be selected with a separated number. For example with `tag=*article 1`.
    `http://localhost:9998/expand?feedUrl=http%3A%2F%2Frss.golem.de%2Frss.php%3Ffeed%3DATOM1.0&include=tag%3D%2Aarticle`
    
  * heise.de feed at heise.de.feedsportal.com/c/35207/f/653902/index.rss can be expanded by including article tag under the element with the id `mitte_news` `id=mitte_news/tag=article`.
    `http://localhost:9998/expand?feedUrl=http%3A%2F%2Fheise.de.feedsportal.com%2Fc%2F35207%2Ff%2F653902%2Findex.rss&include=id%3Dmitte_news%2Ftag%3Darticle`
  As in the golem feed example before it is possible again to select the `article` element directly with simply `tag=*article`
    `http://localhost:9998/expand?feedUrl=http%3A%2F%2Fheise.de.feedsportal.com%2Fc%2F35207%2Ff%2F653902%2Findex.rss&include=tag%3D%2Aarticle`
    
  * Java Code Geeks feeds.feedburner.com/JavaCodeGeeks
  `http://localhost:9998/expand?feedUrl=http%3A%2F%2Ffeeds.feedburner.com%2FJavaCodeGeeks&include=id%3Dmain-content%2Ftag%3Ddiv%2Ftag%3Ddiv`
  
  * Pro Linux pro-linux.de/rss/2/3/rss20_aktuell.xml
  `http://localhost:9998/expand?feedUrl=http%3A%2F%2Fwww.pro-linux.de%2Frss%2F2%2F3%2Frss20_aktuell.xml&include=id%3Dnews`
  
  * Android Police at https://feeds.feedburner.com/AndroidPolice
  `http://localhost:9998/expand?feedUrl=http%3A%2F%2Ffeeds.feedburner.com%2FAndroidPolice&include=id%3Dap-body%2Ftag%3Ddiv+2%2Ftag%3Ddiv+1`
  
  * Asienspiegel http://asienspiegel.ch/feed/
  `http://localhost:9998/expand?feedUrl=http%3A%2F%2Fasienspiegel.ch%2Ffeed%2F&include=id%3Dcontent`
  