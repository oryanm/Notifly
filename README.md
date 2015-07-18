Notifly
=======
Location centered reminders

Road map
========

* ~~extend Application to~~
  * ~~save notes and locations as global vars~~
  * ~~preload addresses~~
* test background service 
* ~~create settings activity~~
* ~~create favorite locations fragment~~
  * ~~show title and address~~ 
  * add order
  * ~~add ability to edit/delete~~
  * ~~add button to create note with a selected location preloaded~~
* ~~enable note editing~~
* add option to sort/order notes and locations
* add color for notes
* ~~setup server backend (google app engine)~~
* add user stats reporting mechanism
* implement machine lerning mechanism
* create widget for the lock screen
* add tags for notes
* improve ui features
* connect to social networks


Learning
========

חישוב זמן יציאה -

•	אלגוריתם שילמד כמה זמן מראש יש להודיע למשתמש. הלמידה תתבסס על מקרים קודמים.
•	הגעה ליעד – לאחר שמזהים שהיוזר בקרבת היעד, נקפיץ הודעה לאישורו וכל עוד הוא לא אישר אך מזהים שהוא לא זז זמן רב, נקבע שאכן זהו היעד.
•	יצאה אל היעד – אישור על ידי היוזר או ע"י כניסה למשימה וסימון שהתבצעה וכל עוד הוא לא סימן, אך כבר הגיע זמן היציאה, נקפיץ הודעה לאישור. במידה ולא אישר לאורך זמן, המשימה נכשלה ולא תיכנס לחישובים.
•	סטטיסטיקות -
תכונות : יום בשבוע, זמן נסיעה (ע"פ הערכת גוגל), אזור בארץ, סוג המשימה (תגית), זמן ביום (בוקר, צהריים, ערב), איחר/לא איחר, זמן איחור, יצא בזמן/לא יצא בזמן, פער הזמן מההודעה ועד שבאמת יצא, זכר/נקבה, אופן תחבורה.
חיפוש קורלציות (pirson correlations) בין התכונות : למשל, בין איחר/לא איחר ובין מרחק, זמן הגעה, אופן תחבורה.
פעם בשבוע (אולי נגדיר אחרת) נריץ אופטימיזציה על פי הנתונים הנ"ל, נחשב כיצד לשנות את הערכות שלנו לגבי היוזר (צריך יותר/פחות זמן התראה לפני משימה מסוג מסויים / בתחבורה מסויימת)
נציג ליוזר גרפים / דו"ח כלשהו.
•	שעון מעורר – על פי עקרון של חישוב זמן יציאה, נוסיף התראה על בסיס של שעון מעורר שתעיר את היוזר בזמן כדי שיתארגן לעבודה/לימודים/משימה כלשהי אחרת- משימה ראשונה של היום.

נוספים :

•	ללמוד מתי כדאי להודיע לו. (למשל, בדרך הביתה)
•	לא להציק למשתמש יותר מדי. למשל אם עבר ליד סופר והודענו לו והוא לא רצה, צריך להחליט מתי נודיע לו פעם הבאה.
•	להגדיר רדיוס חיפוש. (כמה קרוב הוא צריך להיות לסופר כדי שנודיע לו לקנות בו חלב.)



Links
=====

[joda-time-android](https://github.com/dlew/joda-time-android)

[Android apt plugin](https://bitbucket.org/hvisser/android-apt)

[Android Annotations](https://github.com/excilys/androidannotations)

[Dagger](http://square.github.io/dagger/)

[Better Pickers](https://github.com/derekbrameyer/android-betterpickers)

[Swipe List View](https://github.com/47deg/android-swipelistview)

[DragSortListView](https://github.com/bauerca/drag-sort-listview)
* [repo](https://github.com/Goddchen/mvn-repo)

[subtlepatterns](http://subtlepatterns.com)


Google app engine:
------------------
[Backend API Tutorial](https://developers.google.com/appengine/docs/java/endpoints/getstarted/backend/)

[Gradle App Engine plugin](https://github.com/GoogleCloudPlatform/gradle-appengine-plugin)

[Cloud console](https://cloud.google.com/console)



