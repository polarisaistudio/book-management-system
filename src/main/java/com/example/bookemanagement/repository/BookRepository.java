package com.example.bookmanagement.repository;

import com.example.bookmanagement.entity.Book;
import com.example.bookmanagement.entity.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    // Find by ISBN (unique identifier)
    Optional<Book> findByIsbn(String isbn);
    
    // Find books by author
    List<Book> findByAuthorContainingIgnoreCase(String author);
    
    // Find books by title
    List<Book> findByTitleContainingIgnoreCase(String title);
    
    // Find books by status
    List<Book> findByStatus(BookStatus status);
    
    // Find books by price range
    List<Book> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    // Find books by publication year
    List<Book> findByPublicationYear(Integer year);
    
    // Custom JPQL query to find books by multiple criteria
    @Query("SELECT b FROM Book b WHERE " +
           "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:minPrice IS NULL OR b.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR b.price <= :maxPrice)")
    Page<Book> findBooksWithFilters(
            @Param("title") String title,
            @Param("author") String author,
            @Param("status") BookStatus status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
    
    // Native SQL query example
    @Query(value = "SELECT * FROM books WHERE YEAR(created_at) = :year", nativeQuery = true)
    List<Book> findBooksCreatedInYear(@Param("year") Integer year);
    
    // Count books by status
    long countByStatus(BookStatus status);
    
    // Find available books only
    @Query("SELECT b FROM Book b WHERE b.status = 'AVAILABLE' ORDER BY b.title")
    List<Book> findAvailableBooks();
    
    // Check if ISBN already exists (for validation)
    boolean existsByIsbn(String isbn);
    
    // Find books with price greater than average
    @Query("SELECT b FROM Book b WHERE b.price > (SELECT AVG(b2.price) FROM Book b2)")
    List<Book> findBooksAboveAveragePrice();
}
