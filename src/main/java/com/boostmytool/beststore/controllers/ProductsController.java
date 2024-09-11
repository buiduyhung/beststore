package com.boostmytool.beststore.controllers;

import com.boostmytool.beststore.models.Product;
import com.boostmytool.beststore.models.ProductDto;
import com.boostmytool.beststore.services.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping({"", "/"})
    public String showProductList(Model model) {
        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("products", products);
        return "products/index";
    }

    @GetMapping("/create")
    public String showCreatePage(Model model) {
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto", productDto);
        return "products/createProduct";
    }

    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute ProductDto productDto, BindingResult result){
        if (productDto.getImageFileName() == null ||productDto.getImageFileName().isEmpty()){
            result.addError(new FieldError("productDto", "imageFileName", "Image file is required"));
        }
        if (result.hasErrors()){
            return "products/createProduct";
        }

        // save image file
        MultipartFile image = productDto.getImageFileName();
        Date createAt = new Date();
        String storageFileName = createAt.getTime() + "_" + image.getOriginalFilename();

        try{
            String uploadDir = "public/images/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
            }

            try(InputStream inputStream = image.getInputStream()){
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (Exception e){
            System.out.println("Exception" + e.getMessage());
        }

        Product product = new Product();
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setCreatedAt(createAt);
        product.setDescription(productDto.getDescription());
        product.setImageFileName(storageFileName);
        product.setName(productDto.getName());
        product.setPrice(productDto.getPrice());

        productRepository.save(product);

        return "redirect:/products";
    }

    @GetMapping("/edit")
    public String showEditPage(Model model, @RequestParam int id){
        try{
            Product product = productRepository.findById(id).get();
            model.addAttribute("product", product);

            ProductDto productDto = new ProductDto();
            productDto.setBrand(product.getBrand());
            productDto.setCategory(product.getCategory());
            productDto.setDescription(product.getDescription());
            productDto.setName(product.getName());
            productDto.setPrice(product.getPrice());

            model.addAttribute("productDto", productDto);
        }catch (Exception e){
            System.out.println("Exception: " + e.getMessage());
        }

        return "products/editProduct";
    }

    @PostMapping("/edit")
    public String updateProduct(Model model, @RequestParam int id, @Valid @ModelAttribute ProductDto productDto, BindingResult result){

        try{
            Product product = productRepository.findById(id).get();
            model.addAttribute("product", product);

            if (result.hasErrors()){
                return "products/editProduct";
            }

            if (!productDto.getImageFileName().isEmpty()){
                // delete old image file
                String uploadDir = "public/images/";
                Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());

                try{
                    Files.delete(oldImagePath);
                }catch (Exception e){
                    System.out.println("Exception: " + e.getMessage());
                }

                // save new image file
                MultipartFile image = productDto.getImageFileName();
                Date createAt = new Date();
                String storageFileName = createAt.getTime() + "_" + image.getOriginalFilename();

                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
                }
                product.setImageFileName(storageFileName);
            }

            product.setName(productDto.getName());
            product.setBrand(productDto.getBrand());
            product.setCategory(productDto.getCategory());
            product.setName(productDto.getName());

        }catch (Exception e){
            System.out.println("Exception: " + e.getMessage());
        }

        return "redirect:/products";
    }

    @GetMapping("/delete")
    public String deleteProduct(@RequestParam int id){
        try{
            Product product = productRepository.findById(id).get();

            // delete product image
            Path imagePath = Paths.get("public/images" + product.getImageFileName());

            try{
                Files.delete(imagePath);
            }catch (Exception e){
                System.out.println("Exception: " + e.getMessage());
            }

            // delete product
            productRepository.delete(product);
        }catch (Exception e){
            System.out.println("Exception: " + e.getMessage());
        }

        return "redirect:/products";
    }


}























