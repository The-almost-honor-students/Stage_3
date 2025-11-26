package com.tahs.infrastructure.persistence;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.tahs.application.ports.MetadataRepository;
import com.tahs.domain.Book;
import org.bson.Document;

public class MongoMetadataRepository implements MetadataRepository {

    private final MongoDatabase database;
    private final MongoCollection<Document> collection;

    public MongoMetadataRepository(MongoClient mongoClient, String databaseName, String collectionName) {
        this.database = mongoClient.getDatabase(databaseName);
        this.collection = this.database.getCollection(collectionName);
    }

    @Override
    public Book getById(String bookId) {
        Document bookDocument =  this.collection.find((Filters.eq("book_id", Integer.parseInt(bookId)))).first();
        if(bookDocument == null) {
            throw new IllegalArgumentException("Term not found");
        }
        return new Book(
                bookDocument.getInteger("book_id"),
                bookDocument.getString("title"),
                bookDocument.getString("author"),
                bookDocument.getString("language"));
    }
}
