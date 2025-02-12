An excessively detailed simulation of the economy in Hearts of Iron 4
=====================================================================
This project has primarily been created as a Java training project.

The aim is to accurately simulate the economy, and industry in the real time grand strategy game Hearts of Iron 4, in particular, the aim is to simulate a game as Poland, from the games start on the 1st of January 1936, and be ready to fight a defensive war with Nazi Germany on the 14th of September 1938: A year earlier than historically.

This learning project involves the following points:

* A large Java project in IntelliJ with several packages, classes, records, enums, and with error-handling.
* Extensive loading of data from Json files using the Jackson library.
* The organization, generation and display of data in the terminal, and in graphs using the jfree.chart package.


Historical background, and alt-historical changes
-------------------------------------------------
Historically, buoyed by the Anschluss of Austria, Adolf Hitler's demanded to annex the "Sudetenland" region of Czechoslovakia. Though the Czechs were allied to the French, Prime ministers Neville Chamberlain of the UK, and Édouard Daladier agreed with Mussolini and Hitler to pressure the Czechs to hand over the contested region without a fight.

Setting the state for the complete annexation of Czechoslovakia, and the joint Nazi-Soviet invasion of Poland 1 year later.

In the game, there is however an alternative: If Walery Sławek wins the power struggle after the death of the nations founder turned dictator Josef Pilsudski, Poland can restore democracy and form and alliance with Czechoslovakia, Estonia, Latvia and Lithuania before the Munich conference, and fight Hitler already in 1938. (And Stalin 1 year later, when he invades the Baltics)


Mechanical background, Factories in Hearts of Iron 4 works
-----------------------------
The economy in the game follow very simple rules, since the focus of the game is on fighting the war, but even simple rules can be quite complex.

Every nation has a number of "states", these states contains buildings and resources (and people, but that is not important in my simulation). The most important type of building is factories, which can be either Civilian factories (called "Civs" by most players), Military factories (or just "mils"), and Refineries

* Civilian factories are used to build other buildings
    - Buildings cost CIC (Civilian Industrial Capacity) to build 
    - Civilian factories produce 5 CIC per day, which can be used to build buildings, this is increased by 20% pts per level "Infrastructure" in the target state
    - Infrastructure is another building which can be build
    - A number of Civs, proportional to the nations total factories are required for "consumer goods", and do not produce CIC
    - Civs can also be traded for 8 resources, in which case they don't produce
* Military factories are used to build military equipment
    - Military factories produce $4.5\times E\times (1+F) (1-R)$ MIC (Military industrial capacity) each day
    - The efficiency $E$ is a number between 0 and the efficiency cap $E_{cap}<1.0$, and where $dE/dt=0.001 E_{cap}^2/E$ every day (So efficiency grows slowly until it reaches the Efficiency cap)
    - The factories which exist on day 1 start with $E=E_{cap}=0.5$, all new factories start with $E=0.1$
    - $F$ is the factory output bonus for the nation
    - Each Military factory uses a number of resources per factory: either Rubber, Steel, Tungsten, and Chromium (which really represents many rare metals such as Chromium and Nickel), the exact number depend on the type of equipment.
    - For every resource deficit, the resource penalty $R$ grows by 0.05.
    - The MIC is converted to equipment based on its MIC cost, (For example enough guns to equip one infantry squad costs 0.5 MIC, and a single light tank cost 6 MIC)
* Refineries do not produce points, but add synthetic rubber to the states
  - It is not as cost-effective as buying rubber on the free market
* Infrastructure boosts construction speed and gives a 20% boost to local resources
    - The construction speed bonus is generally not worth the investment
    - The resource boost is very valuable in states like Katowice.
* Another type of factory is the naval dockyard, which builds ships
  - Since I am going to focus on Poland, which has a land border with Germany, I will not simulate these

Mechanical background, Stats, "Focuses" and Plan G
------------
Everything is effected by dozens of stats: such as factory output bonus, construction speed bonus, and more, which in turn are effected by political stability, which depends on the popularity of the four great ideologies: Democracy, Fascism, Communism, and non-aligned (which is a miscellaneous category including monarchy, oligarchy, and Finland).

The player can control these stats by researching new technologies, taking "decisions" (often trading one stat for another), and by completing one political "focus" which take 35 or 70 days, and can give stats or instantly create buildings.

A true simulation would include all these things: include the ability to research in different order, and the ability to choose which focus to take.

I have decided not to do this, instead, my simulation loads "events" from a JSon file, and based on these events, modifies stats or state buildings on certain days. After re-playing the start a dozen or so times, I have created "PlanG38.json" which records all the focuses, research, and decisions in what I believe is optimal order. 

I have even tried to make some sort of in-universe narrative explanation of the events, explaining how Walery Sławek (who historically lost the power-struggle to Ignacy Mościcki and Edward Rydz-Śmigły and died in 1939), regained the role as prime-minister, restored democracy, and formed and alliance of minor states against the Nazis.

The goal: building an army for 1938
------------
Poland starts with a modest army, with:

* 18400 infantry equipment
* 209 support equipment

Sidenote: the Game abstracts away a lot of equipment: "1 infantry equipment" really means guns, bullets, uniforms, boots and helmets for 1 infantry squad, and 1 howitzer also includes ammunition for it.


The goal of the economic buildup, from 1936 to 1938 is to build an army to win the war.

The primary task is to defend the Western border in Silesia, and Pommerania and around the Exclave of East-Prussia in the north.

To defend our borders, I estimate that we need 76 infantry divisions.

The game allows the player to design their divisions however they like, the cheapest infantry division (which still has a chance against the 1938 German army) contains:

* 1010 Infantry equipment costs 505 MIC 
* 30 Support equipment costs 120 MIC    
* 12 Howitzers costs 42 MIC             
* 20 Anti-Aircraft guns costs 80 MIC    
* 10900 Men not build in a factory

Or put another way 747 MIC points for a division

so in total our minimum target is:

* 76760 Infantry equipment (58360 on top of existing stockpile)
* 2280 Support equipment   (2071 on top of existing stockpile)
* 912 Howitzers 
* 1520 Anti-Aircraft guns 
* 828400 Men

Costing 56772 MIC (and 0.8 million men, which Poland can easily mobilize).

Additionally, it would be good to have a few hundred trucks (2.5 MIC per truck) and a few dozen armored trains (170 MIC per train) to help with logistics.

It is also worth keeping a few divisions worth of equipment in the stockpile to replace losses.


But wars are not won by defending, and our basic infantry is not good enough for attacking, so we also need a force of elite infantry or cavalry supported by tanks. I expect these elite divisions will need:

* 1300 Infantry equipment costs 650 MIC
* 120 "7TP" light tanks costs 708 MIC (Becomes available in mid 1937)
* 45 Support equipment costs 180 MIC
* 12 Howitzers costs 42 MIC
* 20 Anti-Aircraft guns costs 80 MIC
* 12400 Men not build in a factory

Costing 1660 MIC

This is by no means a good division, compared to the German Panzer divisions, but it is a decent compromise between quality and cost, and I would like at least 6 to invade German Silesia and seize their iron mines.

We will essentially be able to convert one infantry division to an elite division for every 120 tanks, 1990 infantry equipment and 15 support equipment we are above our target.

Results
-----------
Feel free to look at the images included in the repository, which show all stats for the nation and all factories in the best example I ran:

In the course of running the simulation, I found that the best strategy is to start by building Infrastructure in Katowice (to boost the local steel-mines), and then build nothing but military factories, primarily in Katowice and Warzawa (since this is where infrastructure is the highest).

The result is that we build the following factories:

| Operational  | Location      | gained by      | Equipment | total MIC | units |
|--------------|---------------|----------------|-----------|-----------|-------|
| 1st Jan 36   | Gdynia        | owned at start | Support   | 3479      | 869   |
| 1st Jan 36   | Gdynia        | owned at start | Support   | 3479      | 869   |
| 1st Jan 36   | Warsawa       | owned at start | infantry  | 3479      | 6958  |
| 1st Jan 36   | Warsawa       | owned at start | infantry  | 3479      | 6958  |
| 1st Jan 36   | Warsawa       | owned at start | infantry  | 3479      | 6958  |
| 1st Jan 36   | Warsawa       | owned at start | infantry  | 3479      | 6958  |
| 1st Jan 36   | Warsawa       | owned at start | infantry  | 3479      | 6958  |
| 1st Jan 36   | Warsawa       | owned at start | infantry  | 3479      | 6958  |
| 1st Jan 36   | Warsawa       | owned at start | infantry  | 3479      | 6958  |
| 19th Jun 36  | Gdansk/Danzig | From event     | infantry  | 2554      | 5109  |
| 18th Aug 36  | Katowice      | Build          | infantry  | 2453      | 4907  |
| 1st  Oct 36  | Katowice      | Build          | infantry  | 2303      | 4607  |
| 13th Nov 36  | Katowice      | Build          | anti-air  | 2153      | 538   |
| 26th Dec 36  | Katowice      | Build          | anti-air  | 2001      | 500   |
| 4th Feb 37   | Katowice      | Build          | Howitzer  | 1740      | 497   |
| 6th Mar 37   | Gdansk        | From event     | anti-air  | 1756      | 439   |
| 16th Mar 37  | Katowice      | Build          | Howitzer  | 1494      | 426   |
| 3rd May 37   | Warsawa       | Build          | Support   | 1442      | 360   |
| 15th June 37 | Warsawa       | Build          | 7TP Tank  | 1396      | 232   |
| 14th July 37 | Warsawa       | From event     | 7TP Tank  | 1265      | 210   |
| 26th July 37 | Warsawa       | Build          | 7TP Tank  | 1247      | 207   |
| 9th Sep 37   | Lublin        | Build          | Anti-air  | 1085      | 271   |
| 19th Oct 37  | Warsawa       | Build          | Trucks    | 968       | 387   |
| 28th Nov 37  | Warsawa       | Build          | support   | 762       | 190   |
| 25th Jan 38  | Katowice      | Build          | Howitzer  | 493       | 140   |
| 16th Apr 38  | Katowice      | Build          | Trains    | 241       | 2     |
| 8th Aug 38   | Katowice      | Build          | Trains    | 48        | 0     |

In the end, we ended up with:

* 1748 Anti aircraft guns
* 1063 Howitzers
* 386 Trucks
* 2 Armored trains
* 649 7TP tanks
* 2497 support equipment
* 80529 Infantry equipment

This is enough to equip our 74 regular infantry divisions, and 4 Elite divisions, with a modest but adequate logistical train off, 386 Trucks and 2 Armored trains.

* 188 Anti aircraft guns
* 126 Howitzers
* 99 7TP tanks
* 84 support equipment
* 589 Infantry equipment

This is not a big stockpile, and our lack of proper armored trains means the government will have to seize civilian locomotives (The fact that armored trains only become available in 1938 means that we can't do anything about it). 

But we still have built an army more than large enough to defend Poland and Czechoslovakia, and even go on limited counter-offensives with our elite armoured divisions (In fact, with this preparation, the combined Polish and Czech army has numerical parity with the Wehrmacht).

Hitler did not stand a chance. 

In one test-game I did, using the lessons learned from this simulation, the Polish, Czech, and Baltic armies fully occupied all of Germany by Christmas 1939, and promptly turned around and squashed Stalin and Mussolini when they tried to invade Estonia and Greece respectively by 1942. Thus preventing a lot of terrible crimes, and ushering in a new golden age of liberty and democracy, all while suffering only 500000 casualties (only 1/20th of what Poland historically lost in WW2).

This outcome is not particularly surprising, the game is, after all, not balanced around players spending a month mathematically optimizing it!