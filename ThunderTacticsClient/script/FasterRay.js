(function(THREE) {
	var object = null;
	var index = [];
	var divisions = 100;
	var search = 4;
	var divisionsM1 = divisions - 1;
	var offX = null, lenX = null, offZ = null, lenZ = null;
	
	THREE.FasterRay = function (objectMesh, origin, direction) {
		object = objectMesh;
		createFace2dIndex(object.geometry.faces, object.geometry.vertices);
		this.origin = origin || new THREE.Vector3();
		this.direction = direction || new THREE.Vector3();
	};

	var originCopy = new THREE.Vector3();
	var localOriginCopy = new THREE.Vector3();
	var localDirectionCopy = new THREE.Vector3();
	var vector = new THREE.Vector3();
	var normal = new THREE.Vector3();
	var intersectPoint = new THREE.Vector3();
	var inverseMatrix = new THREE.Matrix4();

	var v0 = new THREE.Vector3(), v1 = new THREE.Vector3(), v2 = new THREE.Vector3();
	
	var createFace2dIndex = function(faces, vertices) {
		var minX = 999999;
		var maxX = -999999;
		var minZ = 999999;
		var maxZ = -999999;
		
		var nFaces = faces.length;
		var a, b, c;
		for (var i = 0; i < nFaces; i++) {
			a = vertices[faces[i].a];
			if (a.x < minX) minX = a.x;
			if (a.x > maxX) maxX = a.x;
			if (a.z < minZ) minZ = a.z;
			if (a.z > maxZ) maxZ = a.z;
		}
		
		offX = -minX;
		lenX = maxX - minX;
		offZ = -minZ;
		lenZ = maxZ - minZ;
		
		// Creating 2d array which will contain list of faces.
		for (var i = 0; i < divisions; i++) {
			var xs = [];
			for (var j = 0; j < divisions; j++) {
				xs.push([]);
			}
			index.push(xs);
		}
		
		// The correct way to do it would be to find the circumcenter and the
		// radius of that circle and to add the face to all the cells within
		// that circle.
		
		var face, fx, fz, xx, zz;
		for (var i = 0; i < nFaces; i++) {
			face = faces[i];
			a = vertices[face.a];
			b = vertices[face.b];
			c = vertices[face.c];
			
			xx = (a.x + b.x + c.x) / 3;
			zz = (a.z + b.z + c.z) / 3;
			fx = Math.floor(((xx + offX) / lenX) * divisionsM1);
			fz = Math.floor(((zz + offZ) / lenZ) * divisionsM1);
			
			for (var dx = -search; dx <= search; dx++) {
				for (var dz = -search; dz <= search; dz++) {
					if (fx+dx>=0 && fx+dx<=divisionsM1 &&
							fz+dz>=0 && fz+dz<=divisionsM1) {
						index[fx + dx][fz + dz].push(face);
					}
				}
			}
		}
	};
	
	var pointInFace3 = function ( p, a, b, c ) {
		v0.sub( c, a );
		v1.sub( b, a );
		v2.sub( p, a );

		var dot00 = v0.dot( v0 );
		var dot01 = v0.dot( v1 );
		var dot02 = v0.dot( v2 );
		var dot11 = v1.dot( v1 );
		var dot12 = v1.dot( v2 );

		var invDenom = 1 / ( dot00 * dot11 - dot01 * dot01 );
		var u = ( dot11 * dot02 - dot01 * dot12 ) * invDenom;
		var v = ( dot00 * dot12 - dot01 * dot02 ) * invDenom;

		return ( u >= 0 ) && ( v >= 0 ) && ( u + v < 1 );
	};

	THREE.FasterRay.prototype.getDistance  = function () {
		var f, fl, face, dot, scalar, objMatrix, point, a, b, c;
		var geometry = object.geometry;
		var vertices = geometry.vertices;

		object.matrixRotationWorld.extractRotation( object.matrixWorld );

		originCopy.copy( this.origin );

		objMatrix = object.matrixWorld;
		inverseMatrix.getInverse( objMatrix );

		localOriginCopy.copy( originCopy );
		inverseMatrix.multiplyVector3( localOriginCopy );

		localDirectionCopy.copy( this.direction );
		inverseMatrix.rotateAxis( localDirectionCopy ).normalize();

		var faces, fx, fz;
		fx = Math.floor(((this.origin.x) / lenX) * divisionsM1);
		fz = Math.floor(((this.origin.z) / lenZ) * divisionsM1);
		faces = index[fx][fz];
		
		for (f = 0, fl = faces.length; f < fl; f++) {
			face = faces[f];

			vector.sub(face.centroid, localOriginCopy);
			normal = face.normal;
			dot = localDirectionCopy.dot(normal);

			scalar = normal.dot(vector) / dot;

			intersectPoint.add(localOriginCopy, localDirectionCopy.multiplyScalar(scalar));
			a = vertices[face.a];
			b = vertices[face.b];
			c = vertices[face.c];

			if (pointInFace3(intersectPoint, a, b, c)) {
				point = object.matrixWorld.multiplyVector3(intersectPoint.clone());
				return originCopy.distanceTo(point);
			}
		}
		//Time consuming..
		//console.log("FACE NOT FOUND.");
		return 1000;
	};
}(THREE));