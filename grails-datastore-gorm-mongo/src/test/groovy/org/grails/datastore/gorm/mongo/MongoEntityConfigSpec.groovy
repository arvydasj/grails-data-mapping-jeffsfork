package org.grails.datastore.gorm.mongo

import grails.gorm.tests.GormDatastoreSpec

import org.springframework.data.document.mongodb.MongoTemplate
import org.springframework.datastore.mapping.document.config.DocumentPersistentEntity
import org.springframework.datastore.mapping.model.PersistentEntity
import org.springframework.datastore.mapping.mongo.MongoSession
import org.springframework.datastore.mapping.mongo.config.MongoAttribute
import org.springframework.datastore.mapping.mongo.config.MongoCollection

import spock.lang.Ignore

import com.mongodb.DB
import com.mongodb.WriteConcern

class MongoEntityConfigSpec extends GormDatastoreSpec{
	

//	@Ignore
	def "Test custom collection config"() {
		given:
			session.mappingContext.addPersistentEntity MyMongoEntity
			DB db = session.nativeInterface
			

			db.dropDatabase()
			db.resetIndexCache()
			
		when:
			PersistentEntity entity = session.mappingContext.getPersistentEntity(MyMongoEntity.name)
			
		then:
			entity instanceof DocumentPersistentEntity
			
		when:	
			MongoCollection coll = entity.mapping.mappedForm
			MongoAttribute attr = entity.getPropertyByName("name").getMapping().getMappedForm()
			MongoAttribute location = entity.getPropertyByName("location").getMapping().getMappedForm()
		then:
			coll != null
			coll.collection == 'mycollection'
			coll.database == "test2"
			coll.writeConcern == WriteConcern.FSYNC_SAFE
			attr != null
			attr.index == true
			attr.targetName == 'myattribute'
			attr.indexAttributes == [unique:true]
			location != null
			location.index == true
			location.indexAttributes == [type:"2d"]
			
		when:
			MongoSession ms = session
			MongoTemplate mt = ms.getMongoTemplate(entity)
		then:
			mt.getDefaultCollectionName() == "mycollection"
			
			
	}

}
class MyMongoEntity {
	String id
	
	String name
	String location
	
	static mapping = {
		collection "mycollection"
		database "test2"
		shard "name"
		writeConcern WriteConcern.FSYNC_SAFE
		name index:true, attr:"myattribute", indexAttributes: [unique:true]
		
		location geoIndex:true
	}
}
