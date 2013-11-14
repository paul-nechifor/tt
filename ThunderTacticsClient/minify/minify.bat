cd ../lib
copy /b jquery-1.8.2.min.js+jquery-ui-1.10.0.custom.min.js+three.min.js "../minify/lib.alreadyMinified.js"
copy /b three.domevents.js+THREEx.js "../minify/lib.js"
cd three
copy /b Stats.js "../../minify/lib.three.js"
cd loaders
copy /b ColladaLoader.js+OBJLoader.js "../../../minify/lib.three.loaders.js"
cd ../../../script
copy /b FasterRay.js+constants.js+DynamicObject3D.js+Player.js+ControllablePlayer.js+Map.js+World.js+Shop.js+FightCell.js+Fight.js+Resources.js+ui.js+Inventory.js+main.js "../minify/script.js"
cd ../minify
copy /b lib.js+lib.three.js+lib.three.loaders.js libs-build.js
java -jar yuicompressor-2.4.2.jar libs-build.js -o libs.js
java -jar yuicompressor-2.4.2.jar script.js -o script-min.js
copy /b lib.alreadyMinified.js+libs.js+script-min.js game.min.js

del lib.alreadyMinified.js
del lib.js
del lib.three.js
del lib.three.loaders.js
del script.js
del libs-build.js
del libs.js
del script-min.js