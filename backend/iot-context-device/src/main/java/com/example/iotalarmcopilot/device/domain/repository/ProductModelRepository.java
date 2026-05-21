package com.example.iotalarmcopilot.device.domain.repository;

import com.example.iotalarmcopilot.device.domain.model.ProductCode;
import com.example.iotalarmcopilot.device.domain.model.ProductModel;

import java.util.List;
import java.util.Optional;

/**
 * 产品模型仓储
 */
public interface ProductModelRepository {

    Optional<ProductModel> findByProductCode(ProductCode productCode);

    List<ProductModel> findAll();
}
