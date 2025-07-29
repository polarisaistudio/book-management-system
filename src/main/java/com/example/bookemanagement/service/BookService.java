package com.example.bookmanagement.service;

import com.example.bookmanagement.entity.Book;
import com.example.bookmanagement.entity.BookStatus;
import com.example.bookmanagement.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    // Create a new book
    public Book createBook(Book book) {
        // Check if ISBN already exists
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new RuntimeException("Book with ISBN " + book.getIsbn() + " already exists");
        }
        return bookRepository.save(book);
    }
    
    // Get all books with pagination
    @Transactional(readOnly = true)
    public Page<Book> getAllBooks(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return bookRepository.findAll(pageable);
    }
    
    // Get book by ID
    @Transactional(readOnly = true)
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }
    
    // Get book by ISBN
    @Transactional(readOnly = true)
    public Optional<Book> getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }
    
    // Update book
    public Book updateBook(Long id, Book updatedBook) {
        return bookRepository.findById(id)
            .map(book -> {
                // Check if ISBN is being changed and if new ISBN already exists
                if (!book.getIsbn().equals(updatedBook.getIsbn()) && 
                    bookRepository.existsByIsbn(updatedBook.getIsbn())) {
                    throw new RuntimeException("Book with ISBN " + updatedBook.getIsbn() + " already exists");
                }
                
                book.setTitle(updatedBook.getTitle());
                book.setAuthor(updatedBook.getAuthor());
                book.setIsbn(updatedBook.getIsbn());
                book.setPrice(updatedBook.getPrice());
                book.setPublicationYear(updatedBook.getPublicationYear());
                book.setDescription(updatedBook.getDescription());
                book.setStatus(updatedBook.getStatus());
                
                return bookRepository.save(book);
            })
            .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
    }
    
    // Delete book
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }
    
    // Search books with filters
    @Transactional(readOnly = true)
    public Page<Book> searchBooks(String title, String author, BookStatus status, 
                                 BigDecimal minPrice, BigDecimal maxPrice, 
                                 int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title"));
        return bookRepository.findBooksWithFilters(title, author, status, minPrice, maxPrice, pageable);
    }
    
    // Get books by author
    @Transactional(readOnly = true)
    public List<Book> getBooksByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author);
    }
    
    // Get books by title
    @Transactional(readOnly = true)
    public List<Book> getBooksByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }
    
    // Get available books
    @Transactional(readOnly = true)
    public List<Book> getAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }
    
    // Change book status
    public Book changeBookStatus(Long id, BookStatus newStatus) {
        return bookRepository.findById(id)
            .map(book -> {
                book.setStatus(newStatus);
                return bookRepository.save(book);
            })
            .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
    }
    
    // Get book statistics
    @Transactional(readOnly = true)
    public BookStatistics getBookStatistics() {
        long totalBooks = bookRepository.count();
        long availableBooks = bookRepository.countByStatus(BookStatus.AVAILABLE);
        long checkedOutBooks = bookRepository.countByStatus(BookStatus.CHECKED_OUT);
        long reservedBooks = bookRepository.countByStatus(BookStatus.RESERVED);
        
        return new BookStatistics(totalBooks, availableBooks, checkedOutBooks, reservedBooks);
    }
    
    // Inner class for statistics
    public static class BookStatistics {
        private long totalBooks;
        private long availableBooks;
        private long checkedOutBooks;
        private long reservedBooks;
        
        public BookStatistics(long totalBooks, long availableBooks, long checkedOutBooks, long reservedBooks) {
            this.totalBooks = totalBooks;
            this.availableBooks = availableBooks;
            this.checkedOutBooks = checkedOutBooks;
            this.reservedBooks = reservedBooks;
        }
        
        // Getters
        public long getTotalBooks() { return totalBooks; }
        public long getAvailableBooks() { return availableBooks; }
        public long getCheckedOutBooks() { return checkedOutBooks; }
        public long getReservedBooks() { return reservedBooks; }
    }
}
