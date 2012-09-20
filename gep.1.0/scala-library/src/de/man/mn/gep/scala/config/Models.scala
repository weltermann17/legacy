package de.man.mn.gep.scala.config

object Models {

  val Search = """[
{
"type": "versions",
"displaytype": "Parts",
"icon": "/content/icons/brick.png",
"elements" : [
{
    "name": "Partnumber",
    "displayname": "Partnumber",
    "ui": "combobox",
    "keyfilter": "[*a-zA-Z0-9#_.-]",
    "uppercase": true,
    "length": 13,
    "hint": "<b>Examples:</b><br><br>81.123*<br>*XX*123*<br>*1234<br>81.ABCDE-0123<br>",
    "template": "like&partname&%%1&sort&partname",
    "alias": "Part " 
},
{
    "name": "Change request",
    "displayname": "Change request",
    "ui": "combobox",
    "keyfilter": "[*a-zA-Z0-9#_.-]",
    "uppercase": true,
    "length": 13,
	"hint": "<b>Examples:</b><br><br>33.12345.A<br>33*<br>*.B<br>88.17416.0<br>",
    "template": "like&eco&%%1&sort&lastmodified&sortorder&desc",
    "alias": "Change request " 
},
{
    "name": "Descriptions",
    "displayname": "Descriptions",
    "ui": "combobox",
    "uppercase": true,
    "length": 80,
	"hint": "<b>Examples:</b><br><br>*din*9*<br>buchse*spacing<br>*bracket*<br>*kelepcesi*<br>*rurowa*<br><br>",
    "template": "like&partdescription&%%1&sort&lastmodified&sortorder&desc",
    "alias": "Part description " 
},
{
    "name": "Owner",
    "displayname": "Owner",
    "ui": "combobox",
    "keyfilter": "[*a-zA-Z0-9]",
    "uppercase": true,
    "length": 5,
    "hint": "For example: u01xy, B99CD, *99*, *XZ, P*",
    "template": "like&owner&%%1&sort&lastmodified&sortorder&desc",
    "alias": "Part owner " 
},
{
    "name": "Locked by",
    "displayname": "Locked by",
    "ui": "combobox",
    "keyfilter": "[*a-zA-Z0-9]",
    "uppercase": true,
    "length": 5,
    "hint": "For example: u01xy, B99CD, *99*, *XZ, P*",
    "template": "like&lockedby&%%1&sort&lastmodified&sortorder&desc",
    "alias": "Part locked by " 
},
{
    "name": "Project",
    "displayname": "Project",
    "ui": "combobox",
    "uppercase": true,
    "length": 32,
    "hint": "For example: TRUCK, MV, RX, TGF, R*",
    "template": "like&partproject&%%1&sort&lastmodified&sortorder&desc",
    "alias": "Part project " 
},
{
    "name": "Modified between",
    "displayname": "Modified between",
    "ui": "fromto",
    "hint": "Show all parts modified between oldest and newest date.",
    "template": "between&lastmodified&%%1,%%2&sort&lastmodified&sortorder&desc",
    "alias": "Part modified [%%1, %%2]" 
}, 
{
    "name": "Modified within last",
    "displayname": "Modified within last",
    "ui": "picker",
    "titles": [
        "fifteen minutes",
        "one hour",
        "eight hours",
        "one day",
        "one week",
        "one month",
        "three months",
        "one year" 
    ],
    "values": [
        "now-15",
        "now-60",
        "now-480",
        "today-1",
        "today-7",
        "today-30",
        "today-90",
        "today-365" 
    ],
    "selectedtitle": 1,
    "hint": "Show all parts modified within the selected timespan.",
    "template": "greaterorequal&lastmodified&%%1&sort&lastmodified",
    "alias": "Part modified last " 
},
{
    "name": "Recently accessed",
    "displayname": "Recently accessed parts",
    "ui": null,
    "length": 0,
    "hint": "Search for up to 100 recently accessed parts.",
    "template": null,
    "alias": "Recent parts" 
}
]
},
{
"type": "partnerversions",
"displaytype": "PartnerParts",
"icon": "/content/icons/package_green.png",
"elements" : [
{
    "name": "Partner Partnumber",
    "displayname": "Partnumber",
    "ui": "combobox",
    "length": 80,
    "uppercase": false,
    "hint": "<b>Examples:</b><br><br>*123 abc ABC*<br><br>",
    "template": "like&partnerpartnumber&%%1&sort&partnerpartnumber",
    "alias": "Partnerpart " 
},
{
    "name": "Partner Name",
    "displayname": "Partner Name",
    "ui": "combobox",
    "length": 80,
    "uppercase": true,
    "hint": "<b>Examples:</b><br><br>*123 ABC*<br><br>",
    "template": "like&partnerpartname&%%1&sort&partnerpartname",
    "alias": "Partner Name " 
},
{
    "name": "Description",
    "displayname": "Description",
    "ui": "combobox",
    "uppercase": true,
    "length": 80,
	"hint": "<b>Examples:</b><br><br>*din*9*<br>buchse*spacing<br>*bracket*<br>*kelepcesi*<br>*rurowa*<br><br>",
    "template": "like&partnerpartdescription&%%1&sort&lastmodified&sortorder&desc",
    "alias": "Partnerpart description " 
}
]
},
{
"type": "products",
"displaytype": "Products",
"icon": "/content/icons/lorry.png",
"elements" : [
{
    "name": "Name",
    "displayname": "Name",
    "ui": "combobox",
    "length": 40,
    "uppercase": true,
    "hint": "<b>Examples:</b><br><br>*_PRC<br>*TEST*<br>R14*_LL<br><br>",
    "template": "like&productname&%%1&sort&productname",
    "alias": "Product " 
},
{
    "name": "Descriptions",
    "displayname": "Descriptions",
    "ui": "combobox",
    "uppercase": true,
    "length": 80,
	"hint": "",
    "template": "like&productdescription&%%1&sort&lastmodified&sortorder&desc",
    "alias": "Product description " 
},
{
    "name": "Owner",
    "displayname": "Owner",
    "ui": "combobox",
    "keyfilter": "[*a-zA-Z0-9]",
    "uppercase": true,
    "length": 5,
    "hint": "For example: u01xy, B99CD, *99*, *XZ, P*",
    "template": "like&owner&%%1&sort&lastmodified&sortorder&desc",
    "alias": "Product owner " 
},
{
    "name": "Project",
    "displayname": "Project",
    "ui": "combobox",
    "uppercase": true,
    "length": 32,
    "hint": "For example: TRUCK, MV, RX, TGF, R*",
    "template": "like&productproject&%%1&sort&lastmodified&sortorder&desc",
    "alias": "Product project " 
},
{
    "name": "Modified between",
    "displayname": "Modified between",
    "ui": "fromto",
    "hint": "Show all products modified between oldest and newest date.",
    "template": "between&lastmodified&%%1,%%2&sort&lastmodified&sortorder&desc",
    "alias": "Product modified [%%1, %%2]" 
}, 
{
    "name": "Modified within last",
    "displayname": "Modified within last",
    "ui": "picker",
    "titles": [
        "fifteen minutes",
        "one hour",
        "eight hours",
        "one day",
        "one week",
        "one month",
        "three months",
        "one year" 
    ],
    "values": [
        "now-15",
        "now-60",
        "now-480",
        "today-1",
        "today-7",
        "today-30",
        "today-90",
        "today-365" 
    ],
    "selectedtitle": 1,
    "hint": "Show all products modified within the selected timespan.",
    "template": "greaterorequal&lastmodified&%%1&sort&lastmodified",
    "alias": "Product modified last " 
},
{
    "name": "Recently accessed",
    "displayname": "Recently accessed products",
    "ui": null,
    "length": 0,
    "hint": "Search for up to 100 recently accessed products.",
    "template": null,
    "alias": "Recent products" 
}
]
},
{
"type": "snapshots",
"displaytype": "Snapshots",
"icon": "/content/icons/pictures.png",
"elements" : [
{
    "name": "Name",
    "displayname": "Name",
    "ui": "combobox",
    "length": 80,
    "uppercase": true,
    "hint": "<b>Examples:</b><br><br>*TEST*<br><br>",
    "template": "like&snapshotname&%%1&sort&expires&sortorder&desc",
    "alias": "Snapshot " 
}
]
}
]"""

  val SearchPref =
    """[
{ "url" : "/plm/divisions/truck/subsystems/enovia5/", "title" : "Enovia V5 Truck & Bus (M\u00fcnchen)", "shortname": "TRUCK+BUS V5", "enabled" : true, "selected" : true },
{ "url" : "/plm/divisions/engine/subsystems/enovia5/", "title" : "Enovia V5 Engine (N\u00fcrnberg)", "shortname": "ENGINE V5", "enabled" : true, "selected" : true },
{ "url" : "/plm/divisions/truck/subsystems/enovia4/", "title" : "VPM V4 Truck", "shortname": "MUC V4", "enabled" : false, "selected" : false },
{ "url" : "/plm/divisions/engine/subsystems/enovia4/", "title" : "VPM V4 Engine", "shortname": "NBG V4", "enabled" : false, "selected" : false },
{ "url" : "/plm/divisions/bra/subsystems/enovia4/", "title" : "VPM V4 Latin America", "shortname": "Brazil V4", "enabled" : false, "selected" : false }
]"""

  val PlmServers =
    """[
{ "name" : "No connection", "type" : null, "connectstring" : null },
{ "name" : "Truck & Bus M\u00fcnchen", "type" : "enovia5", "connectstring" : "mndemucenoa:34000" },
{ "name" : "Engine N\u00fcrnberg", "type" : "enovia5", "connectstring" : "mndenbgenoa:34000" }
]"""

}