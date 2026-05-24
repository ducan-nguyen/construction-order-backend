package com.construction.ordersystem.service;

import com.construction.ordersystem.entity.Product;
import com.construction.ordersystem.entity.ProductPriceHistory;
import com.construction.ordersystem.exception.InsufficientStockException;
import com.construction.ordersystem.exception.ResourceNotFoundException;
import com.construction.ordersystem.repository.ProductPriceHistoryRepository;
import com.construction.ordersystem.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductPriceHistoryRepository priceHistoryRepository;

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Page<Product> getActiveProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }

    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByActiveTrueAndNameContainingIgnoreCase(keyword, pageable);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Transactional
    public Product createProduct(Product product) {
        if (product.getStockQuantity() == null) product.setStockQuantity(0);
        Product saved = productRepository.save(product);

        ProductPriceHistory priceHistory = new ProductPriceHistory();
        priceHistory.setProduct(saved);
        priceHistory.setEffectiveDate(LocalDate.now());
        priceHistory.setPrice(saved.getPrice());
        priceHistoryRepository.save(priceHistory);

        return saved;
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setCategory(productDetails.getCategory());
        product.setUnit(productDetails.getUnit());

        if (productDetails.getPrice().compareTo(product.getPrice()) != 0) {
            product.setPrice(productDetails.getPrice());
            ProductPriceHistory priceHistory = new ProductPriceHistory();
            priceHistory.setProduct(product);
            priceHistory.setEffectiveDate(LocalDate.now());
            priceHistory.setPrice(productDetails.getPrice());
            priceHistoryRepository.save(priceHistory);
        }

        product.setStockQuantity(productDetails.getStockQuantity());
        product.setMinOrderQuantity(productDetails.getMinOrderQuantity());
        product.setActive(productDetails.isActive());

        // Giá khuyến mãi — null nếu không có sale hoặc >= giá gốc
        BigDecimal dp = productDetails.getDiscountPrice();
        if (dp != null && dp.compareTo(BigDecimal.ZERO) > 0 && dp.compareTo(product.getPrice()) < 0) {
            product.setDiscountPrice(dp);
        } else {
            product.setDiscountPrice(null);
        }

        return productRepository.save(product);
    }

    @Transactional
    public Product toggleActive(Long id) {
        Product product = getProductById(id);
        product.setActive(!product.isActive());
        return productRepository.save(product);
    }

    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findLowStockProducts(threshold);
    }

    /**
     * Trừ tồn kho với pessimistic lock để tránh race condition khi đặt hàng đồng thời.
     * Phải được gọi trong @Transactional context.
     */
    @Transactional
    public void checkAndUpdateStock(Long productId, int quantity) {
        Product product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Không đủ tồn kho cho sản phẩm: " + product.getName() +
                    ". Còn lại: " + product.getStockQuantity()
            );
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
    }

    public BigDecimal getProductPriceAtDate(Long productId, LocalDate date) {
        Product product = getProductById(productId);
        return priceHistoryRepository.findLatestPriceBeforeDate(product, date)
                .map(ProductPriceHistory::getPrice)
                .orElse(product.getPrice());
    }
}
