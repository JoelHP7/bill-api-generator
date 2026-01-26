# Bill API Generator - English Translation

## Overview
This is the English-translated version of the Bill API Generator project. All classes, methods, variables, comments, and documentation have been translated from Spanish to English for better international collaboration and maintainability.

## Main Changes

### 1. Model/Entity Classes
| Spanish | English |
|---------|---------|
| Cliente | Client |
| ClienteHistorico | ClientHistory |
| Contacto | Contact |
| Factura | Invoice |

### 2. DTOs
| Spanish | English |
|---------|---------|
| ClienteDto | ClientDto |
| ContactoDto | ContactDto |
| FacturaDto | InvoiceDto |
| EmailRequest | EmailRequest (no change) |

### 3. Repositories
| Spanish | English |
|---------|---------|
| ClienteRepository | ClientRepository |
| ClienteHistoricoRepository | ClientHistoryRepository |
| ContactoRepository | ContactRepository |
| FacturaRepository | InvoiceRepository |

### 4. Services
| Spanish | English |
|---------|---------|
| ClienteService | ClientService |
| ClienteHistoricoService | ClientHistoryService |
| ContactoService | ContactService |
| FacturaService | InvoiceService |
| EmailService | EmailService (updated) |
| DocumentGeneratorService | DocumentGeneratorService (updated) |

### 5. Controllers
| Spanish | English |
|---------|---------|
| ClienteController | ClientController |
| ContactoController | ContactController |
| FacturaController | InvoiceController |
| EmailController | EmailController (updated) |

### 6. Database Schema Changes
| Spanish Column | English Column |
|----------------|----------------|
| nombre | name |
| cif | tax_id |
| direccion | address |
| cp | postal_code |
| tarifa | rate |
| horas | hours |
| fecha | date |
| numero_factura | invoice_number |
| numero_secuencial | sequential_number |
| cliente_id | client_id |
| operacion | operation |
| fecha_cambio | change_date |
| telefono | phone |
| cargo | position |
| principal | is_primary |
| imponible | subtotal |
| irpf | tax_withholding |
| iva | vat |
| emisor_* | issuer_* |

### 7. API Endpoints
| Spanish | English |
|---------|---------|
| /api/clientes | /api/clients |
| /api/contactos | /api/contacts |
| /api/facturas | /api/invoices |
| /api/emails | /api/emails (no change) |

## Migration Guide

### Step 1: Database Migration
1. **Backup your database first!**
2. Run the `MIGRATION.sql` script on your database
3. Verify all tables and columns were renamed correctly

```bash
psql -U your_user -d your_database -f MIGRATION.sql
```

### Step 2: Update Your Application
1. Replace the old `src` directory with this translated version
2. Update any custom configurations if needed
3. Build and test your application

```bash
mvn clean install
mvn spring-boot:run
```

### Step 3: Update API Calls
If you have external systems calling your API, update the endpoints:
- `/api/clientes` → `/api/clients`
- `/api/contactos` → `/api/contacts`
- `/api/facturas` → `/api/invoices`

## Important Notes

### Database Compatibility
The migration script is designed for PostgreSQL. If using a different database, adjust accordingly.

### Data Preservation
All your existing data will be preserved during migration. Only table and column names are changed.

### Breaking Changes
⚠️ **Warning:** This is a breaking change if you have:
- External API consumers
- Reports or queries using old table/column names
- Integration tests with hardcoded table names

### Backward Compatibility
The application logic remains the same. Only names have changed.

## Testing

### Run Tests
```bash
mvn test
```

### Manual Testing Checklist
- [ ] Create a new client via `/api/clients`
- [ ] List all clients
- [ ] Create a contact for a client
- [ ] Generate an invoice
- [ ] Send an invoice via email
- [ ] Check client history
- [ ] Verify database tables and columns

## Benefits of Translation

1. **Better International Collaboration**: English is the standard in software development
2. **Improved Code Readability**: Consistent language throughout the codebase
3. **Easier Onboarding**: New developers can understand the code faster
4. **Better Documentation**: English documentation is more accessible
5. **Professional Standards**: Follows industry best practices

## Support

If you encounter any issues during migration:
1. Check the database migration completed successfully
2. Verify all environment variables are correctly set
3. Review application logs for any errors
4. Ensure all dependencies are up to date

## File Structure
```
src/
├── main/
│   ├── java/com/bill_api_generator/bill_api_generator/
│   │   ├── controller/
│   │   │   ├── ClientController.java
│   │   │   ├── ContactController.java
│   │   │   ├── InvoiceController.java
│   │   │   └── EmailController.java
│   │   ├── dto/
│   │   │   ├── ClientDto.java
│   │   │   ├── ContactDto.java
│   │   │   ├── InvoiceDto.java
│   │   │   └── EmailRequest.java
│   │   ├── model/
│   │   │   ├── Client.java
│   │   │   ├── ClientHistory.java
│   │   │   ├── Contact.java
│   │   │   └── Invoice.java
│   │   ├── repository/
│   │   │   ├── ClientRepository.java
│   │   │   ├── ClientHistoryRepository.java
│   │   │   ├── ContactRepository.java
│   │   │   └── InvoiceRepository.java
│   │   ├── service/
│   │   │   ├── ClientService.java
│   │   │   ├── ClientHistoryService.java
│   │   │   ├── ContactService.java
│   │   │   ├── InvoiceService.java
│   │   │   ├── EmailService.java
│   │   │   └── DocumentGeneratorService.java
│   │   └── BillApiGeneratorApplication.java
│   └── resources/
│       ├── application.yml
│       ├── data.sql
│       └── templates/
│           └── plantilla_v2.docx
└── test/
    └── java/com/bill_api_generator/bill_api_generator/
        ├── TestBillApiGeneratorApplication.java
        └── TestcontainersConfiguration.java
```

## Version
- Original Version: Spanish
- Translated Version: English
- Date: 2026-01-24

---
**Translation completed successfully!** 🎉
