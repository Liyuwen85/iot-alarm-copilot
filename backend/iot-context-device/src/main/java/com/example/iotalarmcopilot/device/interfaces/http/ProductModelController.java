package com.example.iotalarmcopilot.device.interfaces.http;

import com.example.iotalarmcopilot.device.application.ProductModelQueryApplicationService;
import com.example.iotalarmcopilot.device.application.ProductModelVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 产品模型接口
 */
@RestController
@RequestMapping("/api/product-models")
public class ProductModelController {

    private final ProductModelQueryApplicationService productModelQueryApplicationService;

    public ProductModelController(ProductModelQueryApplicationService productModelQueryApplicationService) {
        this.productModelQueryApplicationService = productModelQueryApplicationService;
    }

    @GetMapping
    public List<ProductModelVO> all() {
        return productModelQueryApplicationService.all();
    }

    @GetMapping("/{productCode}")
    public ProductModelVO get(@PathVariable("productCode") String productCode) {
        return productModelQueryApplicationService.get(productCode);
    }
}
