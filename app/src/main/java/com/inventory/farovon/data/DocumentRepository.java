package com.inventory.farovon.data;

import com.inventory.farovon.model.Asset;
import com.inventory.farovon.model.IssueDocument;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DocumentRepository {
    private static volatile DocumentRepository instance;
    private final List<IssueDocument> documents = new ArrayList<>();

    private DocumentRepository() {
        // Add some initial dummy data
        List<Asset> assets1 = new ArrayList<>();
        assets1.add(new Asset("Ноутбук Dell", "INV001", "SN001", "Склад 1", "Технопарк", "Выдан"));
        assets1.add(new Asset("Монитор Samsung", "INV002", "SN002", "Склад 1", "Технопарк", "Выдан"));
        documents.add(new IssueDocument("1", new Date(), "Петров И.И.", "IT-отдел", "Технопарк", "Склад 1", "Иванов И.И.", "Бухгалтерия", "Технопарк", "Кабинет 101", "Проведен", assets1));

        List<Asset> assets2 = new ArrayList<>();
        assets2.add(new Asset("Клавиатура", "INV003", "SN003", "Склад 1", "Технопарк", "Выдан"));
        documents.add(new IssueDocument("2", new Date(), "Сидоров А.А.", "Склад", "Технопарк", "Склад 1", "Смирнов К.К.", "Отдел кадров", "Технопарк", "Кабинет 205", "К выполнению", assets2));
    }

    public static DocumentRepository getInstance() {
        if (instance == null) {
            synchronized (DocumentRepository.class) {
                if (instance == null) {
                    instance = new DocumentRepository();
                }
            }
        }
        return instance;
    }

    public List<IssueDocument> getDocuments() {
        return documents;
    }

    public void addDocument(IssueDocument document) {
        documents.add(document);
    }

    public IssueDocument getDocumentById(String id) {
        for (IssueDocument doc : documents) {
            if (doc.getNumber().equals(id)) {
                return doc;
            }
        }
        return null;
    }
}
