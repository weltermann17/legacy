package de.man.mn.gep.scala.config

object Layouts {

  object SearchResults {

    val Versions =
      """[{"name":"row","type":"integer","title":"#","width":"50px","align":"left"} 
,{"name":"name","type":"text","title":"Partnumber","width":"120px","align":"left","cansort":true,"cangroupby":true} 
,{"name":"versionstring","type":"text","title":"Version","width":"50px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"changerequest","type":"text","islink":true,"linktype":"remotesearch","title":"Change request","width":"90px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"description_de","type":"description","title":"Description","width":"*","align":"left","iconurl":"/content/flags/24/GM.png","cansort":true,"cangroupby":true} 
,{"name":"status","type":"text","title":"Status","width":"70px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"owner","type":"text","islink":true,"linktype":"user","title":"Owner","width":"60px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"lockowner","type":"text","islink":true,"linktype":"user","title":"Locked by","width":"60px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"lastmodified","type":"date","title":"Last modified","width":"80px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"isassembly","type":"boolean","title":"Assembly","width":"60px","align":"center"} 
,{"name":"isstandardpart","type":"boolean","title":"Standard","width":"60px","align":"center"} 
,{"name":"id","primarykey":true,"type":"text","hidden":true} 
]"""

    val Bom =
      """[{"name":"level","type":"integer","title":"Level","width":"40px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"name","type":"text","title":"Partnumber (Child)","width":"120px","align":"left","cansort":true,"cangroupby":true} 
,{"name":"versionstring","type":"text","title":"Version","width":"60px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"quantity","type":"integer","title":"Quantity","width":"50px","align":"center"} 
,{"name":"changerequest","type":"text","islink":true,"title":"Change request","width":"90px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"description_de","type":"description","title":"Description","width":"*","align":"left","iconurl":"/content/flags/24/GM.png","cansort":true,"cangroupby":true} 
,{"name":"status","type":"text","title":"Status","width":"70px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"owner","type":"text","islink":true,"linktype":"user","title":"Owner","width":"60px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"lockowner","type":"text","islink":true,"linktype":"user","title":"Locked by","width":"60px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"lastmodified","type":"date","title":"Last modified","width":"80px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"isassembly","type":"boolean","title":"Assembly","width":"60px","align":"center"} 
,{"name":"isstandardpart","type":"boolean","title":"Standard","width":"60px","align":"center"} 
,{"name":"id","primarykey":true,"type":"text","hidden":true} 
]"""

    val WhereUsed =
      """[{"name":"level","type":"integer","title":"Level","width":"40px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"name","type":"text","title":"Partnumber (Parent)","width":"120px","align":"left","cansort":true,"cangroupby":true} 
,{"name":"versionstring","type":"text","title":"Version","width":"50px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"quantity","type":"integer","title":"Quantity","width":"50px","align":"center"} 
,{"name":"changerequest","type":"text","islink":true,"title":"Change request","width":"90px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"description_de","type":"description","title":"Description","width":"*","align":"left","iconurl":"/content/flags/24/GM.png","cansort":true,"cangroupby":true} 
,{"name":"status","type":"text","title":"Status","width":"70px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"owner","type":"text","islink":true,"linktype":"user","title":"Owner","width":"60px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"lockowner","type":"text","islink":true,"linktype":"user","title":"Locked by","width":"60px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"lastmodified","type":"date","title":"Last modified","width":"80px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"isassembly","type":"boolean","title":"Assembly","width":"60px","align":"center"} 
,{"name":"isstandardpart","type":"boolean","title":"Standard","width":"60px","align":"center"} 
,{"name":"id","primarykey":true,"type":"text","hidden":true} 
]"""

    val Instances =
      """[{"name":"row","type":"integer","title":"#","width":"50px","align":"left"} 
,{"name":"instance","type":"text","title":"Instance","width":"100px","align":"left","cansort":true,"cangroupby":true} 
,{"name":"hidden","type":"boolean","title":"Hide 3D","width":"60px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"project2d","type":"boolean","title":"Hide 2D","width":"60px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"parent","type":"text","title":"Parent","width":"120px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"description_de","type":"description","title":"Description (Parent)","width":"*","align":"left","iconurl":"/content/flags/24/GM.png","cansort":true,"cangroupby":true} 
,{"name":"level","type":"integer","title":"Level","width":"40px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"product","type":"text","title":"Product","width":"280px","align":"left","cansort":true,"cangroupby":true} 
,{"name":"lockowner","type":"text","islink":true,"linktype":"user","title":"Locked by","width":"60px","align":"center","cansort":true,"cangroupby":true,"hidden":false} 
,{"name":"id","primarykey":true,"type":"text","hidden":true} 
]"""

    val Products =
      """[{"name":"row","type":"integer","title":"#","width":"50px","align":"left"} 
,{"name":"name","type":"text","title":"Product","width":"280px","align":"left","cansort":true,"cangroupby":true} 
,{"name":"description_de","type":"description","title":"Description","width":"*","align":"left","cansort":true,"cangroupby":true} 
,{"name":"lastmodified","type":"date","title":"Last modified","width":"80px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"owner","type":"text","islink":true,"linktype":"user","title":"Owner","width":"60px","align":"center","cansort":true,"cangroupby":true}
,{"name":"id","primarykey":true,"type":"text","hidden":true} 
]"""

    val Snapshots =
      """[{"name":"parentname","type":"text","title":"Parent","width":"160px","align":"left","cansort":true,"cangroupby":true,"canedit":false} 
,{"name":"name","type":"text","title":"Name","width":"160px","align":"left","cansort":true,"cangroupby":true,"canedit":true,"required":true} 
,{"name":"description_de","type":"description","title":"Description","width":"*","align":"left","cansort":true,"cangroupby":true,"canedit":true} 
,{"name":"storage","type":"text","title":"Storage","width":"80px","align":"center","cansort":true,"cangroupby":true,"canedit":false} 
,{"name":"iterationstagged","type":"integer","title":"Tagged","width":"60px","align":"center","cansort":true,"cangroupby":true,"canedit":false} 
,{"name":"lastmodified","type":"date","title":"Last modified","width":"80px","align":"center","cansort":true,"cangroupby":true,"canedit":false} 
,{"name":"expirationdate","type":"date","title":"Expires","width":"100px","align":"center","cansort":true,"cangroupby":true,"canedit":true,"required":true,"minimum":30,"maximum":1096,"message":"Expiration date must be within 30 days and 3 years from today."} 
,{"name":"owner","type":"text","islink":true,"linktype":"user","title":"Owner","width":"60px","align":"center","cansort":true,"cangroupby":true,"canedit":false,"required":true}
,{"name":"lockowner","type":"text","islink":true,"linktype":"user","title":"Locked by","width":"60px","align":"center","cansort":true,"cangroupby":true,"hidden":false,"canedit":false} 
,{"name":"id","primarykey":true,"type":"text","hidden":true,"canedit":false}
]"""

    val Partners =
      """[{"name":"row","type":"integer","title":"#","width":"50px","align":"left"} 
,{"name":"name","type":"text","title":"Partner","width":"110px","align":"left","cansort":true,"cangroupby":true} 
,{"name":"system","type":"text","title":"System","width":"60px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"partnumber","type":"text","title":"Partnumber","width":"120px","align":"left","cansort":true,"cangroupby":true} 
,{"name":"versionstring","type":"text","title":"Version","width":"60px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"mansystem","type":"text","title":"MAN","width":"60px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"manpartnumber","type":"text","title":"Partnumber","width":"120px","align":"left","cansort":true,"cangroupby":true} 
,{"name":"manversionstring","type":"text","title":"Version","width":"60px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"description","type":"description","title":"Description","width":"*","align":"left","iconurl":"/content/flags/24/GM.png","cansort":true,"cangroupby":true} 
,{"name":"owner","type":"text","islink":true,"linktype":"user","title":"Owner","width":"60px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"lastmodified","type":"date","title":"Last modified","width":"80px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"id","primarykey":true,"type":"text","hidden":true} 
]"""

    val PartnerVersions =
      """[{"name":"versionname","type":"text","title":"MAN Part","width":"110px","align":"left","cansort":true,"cangroupby":true}
,{"name":"partnername","type":"enum","title":"Partner Name","width":"110px","align":"left","cansort":true,"cangroupby":true,"canedit":true,"required":true}
,{"name":"name","type":"text","title":"Partnumber","width":"110px","align":"left","cansort":true,"cangroupby":true,"canedit":true,"required":true} 
,{"name":"description_de","type":"description","title":"Description","width":"*","align":"left","iconurl":"/content/flags/24/GM.png","cansort":true,"cangroupby":true,"canedit":true}
,{"name":"revisionstring","type":"text","title":"Revision","width":"60px","align":"left","cansort":true,"cangroupby":true,"canedit":true}
,{"name":"team","type":"text","title":"Organization","width":"60px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"owner","type":"text","islink":true,"linktype":"user","title":"Owner","width":"60px","align":"center","cansort":true,"cangroupby":true} 
,{"name":"lastmodified","type":"date","title":"Last modified","width":"80px","align":"center","cansort":true,"cangroupby":true}
,{"name":"partner","type":"text","title":"Partner UUID","canedit":true,"hidden":true,"required":true} 
,{"name":"id","primarykey":true,"type":"text","hidden":true} 
]"""

  }

  object Details {

    val Versions =
      """[
{"name":"name","type":"text","title":"Partnumber","prompt":"Search for this partnumber.","bold":true,"islink":true} 
,{"name":"versionstring","type":"text","title":"Version","bold":true} 
,{"type":"spacer"}
,{"name":"changerequest","type":"text","islink":true,"title":"Change request","prompt":"Search for this change request."} 
,{"name":"status","type":"text","title":"Status"} 
,{"name":"owner","type":"text","islink":true,"title":"Owner","prompt":"Search for this owner."} 
,{"name":"lockowner","type":"text","islink":true,"title":"Locked by","prompt":"Search for this lockowner."} 
,{"name":"pdm","type":"text","title":"Pdm"} 
,{"name":"team","type":"text","title":"Team"} 
,{"name":"project","type":"text","title":"Project","islink":true,"prompt":"Search for this project.","category":"Project"} 
,{"name":"lastmodified","type":"date","title":"Last modified"} 
,{"name":"created","type":"date","title":"Created"} 
,{"name":"material","type":"text","title":"Material"} 
,{"name":"weight","type":"text","title":"Weight (approx.)","hint":"kg"} 
,{"name":"isassembly","type":"boolean","title":"Assembly"} 
,{"name":"isstandardpart","type":"boolean","title":"Standard part"} 
,{"type":"spacer"}
,{"name":"instances","type":"text","title":"Instance children"} 
,{"name":"parts","type":"text","title":"Part children"} 
,{"name":"parents","type":"text","title":"Where used"} 
,{"type":"spacer"}
,{"name":"description_de","type":"description","title":"Description"} 
]"""

    val Instances =
      """[
{"name":"instance","type":"text","span":true,"title":"Instance","bold":true}
,{"name":"lockowner","type":"text","islink":true,"title":"Locked by","prompt":"Search for this lockowner."} 
,{"name":"hidden","type":"boolean","title":"Hide 3D"}
,{"name":"hiddenbyparent","type":"boolean","title":"Has hidden parent"}
,{"name":"level","type":"text","title":"Level"}
,{"type":"spacer"}
,{"name":"name","type":"text","title":"Partnumber","category":"Partnumber","datatype":"versions","prompt":"Search for this partnumber.","islink":true,"bold":true}
,{"name":"versionstring","type":"text","title":"Version","bold":true}
,{"name":"isassembly","type":"boolean","title":"Assembly"} 
,{"name":"project2d","type":"boolean","title":"Hide 2D"}
,{"type":"spacer"}
,{"name":"instances","type":"text","title":"Instance children"} 
,{"name":"parts","type":"text","title":"Part children"} 
,{"type":"spacer"}
,{"name":"parentname","type":"text","title":"Partnumber (parent)","category":"Partnumber","datatype":"versions","prompt":"Search for this partnumber.","islink":true,"bold":true}
,{"name":"parentversion","type":"text","title":"Version (parent)","bold":true}
,{"type":"spacer"}
,{"name":"description_de","type":"description","title":"Description (parent)"} 
,{"type":"spacer"}
,{"name":"product","type":"text","span":true,"title":"Product","category":"Product","datatype":"products","prompt":"Search for this product.","bold":true,"islink":true}
]"""

    val Products =
      """[
{"name":"name","type":"text","span":true,"title":"Product","prompt":"Search for this product.","bold":true,"islink":true}
,{"type":"spacer"}
,{"name":"owner","type":"text","islink":true,"title":"Owner","prompt":"Search for this owner."}
,{"name":"lockowner","type":"text","islink":true,"title":"Locked by","prompt":"Search for this lockowner."}
,{"name":"pdm","type":"text","title":"Pdm"} 
,{"name":"team","type":"text","title":"Team"} 
,{"name":"project","type":"text","title":"Project","islink":true,"prompt":"Search for this project.","category":"Project"} 
,{"name":"lastmodified","type":"date","title":"Last modified"} 
,{"name":"created","type":"date","title":"Created"}
,{"type":"spacer"}
,{"name":"instances","type":"text","title":"Instance children"} 
,{"name":"parts","type":"text","title":"Part children"} 
,{"type":"spacer"}
,{"name":"description_de","type":"description","title":"Description DE"} 
]"""

    val Snapshots =
      """[
{"name":"name","type":"text","span":true,"title":"Snapshot","prompt":"Search for this snapshot.","bold":true,"islink":true,"category":"Snapshot"}
,{"type":"spacer"}
,{"name":"owner","type":"text","islink":true,"title":"Owner","prompt":"Search for this owner."}
,{"name":"lockowner","type":"text","islink":true,"title":"Locked by","prompt":"Search for this lockowner."}
,{"name":"pdm","type":"text","title":"Pdm"} 
,{"name":"team","type":"text","title":"Team"} 
,{"name":"project","type":"text","title":"Project","islink":true,"prompt":"Search for this project.","category":"Project"} 
,{"name":"expirationdate","type":"date","title":"Expires"} 
,{"name":"lastmodified","type":"date","title":"Last modified"} 
,{"name":"created","type":"date","title":"Created"}
,{"type":"spacer"}
,{"name":"storage","type":"text","title":"Storage (mb)"} 
,{"name":"iterationstagged","type":"text","title":"Tagged iterations"} 
,{"type":"spacer"}
,{"name":"parentname","type":"text","title":"Parent","prompt":"Search for this parent.","islink":true,"datatype":"versions","category":"Partnumber"} 
,{"type":"spacer"}
,{"name":"description_de","type":"description","title":"Description DE"} 
]"""

    val Partners =
      """[
{"name":"name","type":"text","span":true,"title":"Snapshot","prompt":"Search for this snapshot.","bold":true,"islink":true,"category":"Snapshot"}
,{"type":"spacer"}
,{"name":"owner","type":"text","islink":true,"title":"Owner","prompt":"Search for this owner."}
,{"name":"lockowner","type":"text","islink":true,"title":"Locked by","prompt":"Search for this lockowner."}
,{"name":"pdm","type":"text","title":"Pdm"} 
,{"name":"team","type":"text","title":"Team"} 
,{"name":"project","type":"text","title":"Project","islink":true,"prompt":"Search for this project.","category":"Project"} 
,{"name":"lastmodified","type":"date","title":"Last modified"} 
,{"name":"created","type":"date","title":"Created"}
,{"type":"spacer"}
,{"name":"description_de","type":"description","title":"Description DE"} 
]"""

    val PartnerVersions =
      """[
{"name":"name","type":"text","span":true,"title":"Partner Part","bold":true}
,{"type":"spacer"}
,{"name":"versionname","type":"text","title":"MAN Part"}
,{"name":"partnername","type":"text","title":"Partner Name"}
,{"name":"owner","type":"text","islink":true,"title":"Owner","prompt":"Search for this owner."}
,{"name":"lockowner","type":"text","islink":true,"title":"Locked by","prompt":"Search for this lockowner."}
,{"name":"team","type":"text","title":"Team"} 
,{"name":"project","type":"text","title":"Project","islink":true,"prompt":"Search for this project.","category":"Project"} 
,{"name":"lastmodified","type":"date","title":"Last modified"} 
,{"type":"spacer"}
,{"name":"description_de","type":"description","title":"Description DE"} 
]"""

  }

  object Menus {

    val Versions =
      """[
{"type":"item","title":"Show #displayname in column view","icon":"/content/icons/timeline_marker.png","action":{"type":"millertree","title":"Column view"}} 
,{"type":"item","title":"Show #displayname in tree view","icon":"/content/icons/chart_organisation.png","action":{"type":"spacetree","title":"Tree view"}} 
,{"type":"spacer"} 
,{"type":"item","title":"Show #displayname bill of material","icon":"/content/icons/arrow_join.png","conditions":{"isassembly":true},"action":{"type":"search","datatype":"bom","title":"Bill of material"}} 
,{"type":"item","title":"Show #displayname whereused","icon":"/content/icons/arrow_divide.png","action":{"type":"search","datatype":"whereused","title":"Where used"}} 
,{"type":"spacer"} 
,{"type":"item","title":"Show #displayname all instances","icon":"/content/icons/bricks.png","action":{"type":"search","datatype":"instances","title":"All instances"}} 
,{"type":"item","title":"Show #displayname all products","icon":"/content/icons/lorry.png","action":{"type":"search","datatype":"products","title":"All products"}} 
,{"type":"item","title":"Show #displayname all snapshots","icon":"/content/icons/pictures.png","action":{"type":"search","datatype":"snapshots","title":"All snapshots"}} 
,{"type":"item","title":"Show #displayname all partner parts","icon":"/content/icons/package_green.png","action":{"type":"search","datatype":"partnerversions","title":"Partner part"}} 
,{"type":"spacer","conditions":{"isassembly":true}}
,{"type":"item","title":"Create new snapshot for #displayname","icon":"/content/icons/pictures.png","conditions":{"isassembly":true},"action":{"type":"create","datatype":"snapshots","title":"Create new snapshot"}}
,{"type":"spacer"}
,{"type":"item","title":"Create new partner part for #displayname","icon":"/content/icons/pictures.png","action":{"type":"create","datatype":"partnerversions","title":"Create new partner part"}}
,{"type":"spacer","conditions":{"isassembly":false}} 
,{"type":"item","title":"Open #displayname in Catia V5 from Enovia V5","icon":"/content/icons/CATPart.png","conditions":{"isassembly":false},"action":{"type":"enovia5"}} 
]"""

    val Instances =
      """[
{"type":"item","title":"Show part for #displayname","icon":"/content/icons/brick.png","action":{"type":"search","datatype":"versions","title":"Part"}} 
,{"type":"item","title":"Show product for #displayname","icon":"/content/icons/lorry.png","action":{"type":"search","datatype":"products","title":"Product"}} 
,{"type":"spacer"} 
,{"type":"item","title":"Show #displayname bill of material","icon":"/content/icons/arrow_join.png","conditions":{"isassembly":true},"action":{"type":"search","datatype":"bom","title":"Bill of material"}} 
,{"type":"item","title":"Show #displayname whereused","icon":"/content/icons/arrow_divide.png","action":{"type":"search","datatype":"whereused","title":"Where used"}} 
,{"type":"item","title":"Show #displayname all snapshots","icon":"/content/icons/pictures.png","action":{"type":"search","datatype":"snapshots","title":"All snapshots"}} 
,{"type":"spacer","conditions":{"isassembly":true}}
,{"type":"item","title":"Create new snapshot for #displayname","icon":"/content/icons/pictures.png","conditions":{"isassembly":true},"action":{"type":"create","datatype":"snapshots","title":"Create new snapshot"}}
,{"type":"spacer","conditions":{"isassembly":false}} 
,{"type":"item","title":"Open #displayname in Catia V5 from Enovia V5","icon":"/content/icons/CATPart.png","conditions":{"isassembly":false},"action":{"type":"enovia5"}} 
,{"type":"spacer","conditions":{"isassembly":true}} 
,{"type":"item","title":"Open #displayname in Catia V5 from Enovia V5","icon":"/content/icons/CATProduct.png","conditions":{"isassembly":true},"action":{"type":"enovia5"}} 
]"""

    val Products =
      """[
{"type":"item","title":"Show #displayname in column view","icon":"/content/icons/timeline_marker.png","action":{"type":"millertree","title":"Column view"}} 
,{"type":"item","title":"Show #displayname in tree view","icon":"/content/icons/chart_organisation.png","action":{"type":"spacetree","title":"Tree view"}} 
,{"type":"spacer"} 
,{"type":"item","title":"Show #displayname bill of material","icon":"/content/icons/arrow_join.png","conditions":{"isassembly":true},"action":{"type":"search","datatype":"bom","title":"Bill of material"}} 
,{"type":"item","title":"Show #displayname all snapshots","icon":"/content/icons/pictures.png","action":{"type":"search","datatype":"snapshots","title":"All snapshots"}} 
,{"type":"spacer"} 
,{"type":"item","title":"Create new snapshot for #displayname","icon":"/content/icons/pictures.png","action":{"type":"create","datatype":"snapshots","title":"Create new snapshot"}}
,{"type":"spacer"} 
,{"type":"item","title":"Open #displayname in Catia V5 from Enovia V5","icon":"/content/icons/CATProduct.png","action":{"type":"enovia5"}} 
]"""

    val Snapshots =
      """[
{"type":"item","title":"Show part for #displayname","icon":"/content/icons/brick.png","conditions":{"parenttype":"versions"},"action":{"type":"search","datatype":"versions","title":"Part"}} 
,{"type":"item","title":"Show instance for #displayname","icon":"/content/icons/bricks.png","conditions":{"parenttype":"instances"},"action":{"type":"search","datatype":"instances","title":"Instance"}} 
,{"type":"item","title":"Show product for #displayname","icon":"/content/icons/lorry.png","conditions":{"parenttype":"products"},"action":{"type":"search","datatype":"products","title":"Products"}} 
,{"type":"spacer"} 
,{"type":"item","title":"Show #displayname in column view","icon":"/content/icons/timeline_marker.png","action":{"type":"millertree","title":"Column view"}} 
,{"type":"item","title":"Show #displayname in tree view","icon":"/content/icons/chart_organisation.png","action":{"type":"spacetree","title":"Tree view"}} 
,{"type":"spacer"} 
,{"type":"item","title":"Show #displayname bill of material","icon":"/content/icons/arrow_join.png","conditions":{"isassembly":true},"action":{"type":"search","datatype":"bom","title":"Bill of material"}} 
,{"type":"item","title":"Show #displayname whereused","icon":"/content/icons/arrow_divide.png","action":{"type":"search","datatype":"whereused","title":"Where used"}} 
,{"type":"spacer","conditions":{"lockowner":true}} 
,{"type":"item","title":"Edit #displayname","icon":"/content/icons/picture_edit.png","conditions":{"lockowner":true},"action":{"type":"edit","datatype":"snapshots"}} 
,{"type":"item","title":"Delete #displayname","icon":"/content/icons/picture_delete.png","conditions":{"lockowner":true},"action":{"type":"delete","datatype":"snapshots"}} 
,{"type":"spacer","conditions":{"lockowner":false}} 
,{"type":"item","title":"Lock #displayname","icon":"/content/icons/lock.png","conditions":{"lockowner":false},"action":{"type":"lock","datatype":"snapshots"}} 
,{"type":"spacer","conditions":{"lockowner":true}} 
,{"type":"item","title":"Unlock #displayname","icon":"/content/icons/lock_open.png","conditions":{"lockowner":true},"action":{"type":"unlock","datatype":"snapshots"}} 
]"""

    val Partners =
      """[
]"""

    val PartnerVersions =
      """[
{"type":"item","title":"Delete partner part #displayname","icon":"/content/icons/picture_delete.png","action":{"type":"delete","datatype":"partnerversions"}} 
]"""

  }
}

