package com.likelion.backend.domain.lab.service;

import com.likelion.backend.domain.lab.entity.BaseProduct;
import java.util.List;
import java.util.stream.IntStream;

/** 파일: src/main/resources/static/lab-editions/{slug}-1.png .. -4.png */
public final class LabEditionCatalog {

  public static final String IMAGE_DIR = "/lab-editions/";
  public static final int IMAGE_COUNT = 4;

  public static final List<Item> ITEMS =
      List.of(
          new Item(
              BaseProduct.STARK_SIDE_STUDS_BACKPACK,
              "Stark Side Studs Backpack",
              "Summer Remix",
              "stark-side-studs",
              "Sand",
              "M",
              2,
              1_350_000),
          new Item(
              BaseProduct.TRACY_SATCHEL,
              "Tracy Satchel Bag",
              "Summer Remix",
              "tracy-satchel",
              "Sand",
              "S",
              2,
              1_250_000),
          new Item(
              BaseProduct.AREN_VANITY_CASE,
              "Aren Vanity Case Bag",
              "Summer Remix",
              "aren-vanity-case",
              "Sand",
              "S",
              2,
              1_250_000),
          new Item(
              BaseProduct.ELLA_BOSTON_BAG,
              "Ella Boston Bag",
              "Summer Remix",
              "ella-boston",
              "Sand",
              "S",
              2,
              1_250_000),
          new Item(
              BaseProduct.PINA_TAMBOURINE_BAG,
              "Pina Tambourine Bag",
              "Summer Remix",
              "pina-tambourine",
              "Sand",
              "S",
              2,
              1_250_000),
          new Item(
              BaseProduct.TONI_TOP_ZIP_SHOPPER,
              "Toni Top-Zip Shopper Bag",
              "Summer Remix",
              "toni-top-zip-shopper",
              "Sand",
              "M",
              2,
              1_250_000));

  private LabEditionCatalog() {}

  public static String primaryImageUrl(String slug) {
    return IMAGE_DIR + slug + "-1.png";
  }

  public static List<String> imageUrls(String slug) {
    return IntStream.rangeClosed(1, IMAGE_COUNT)
        .mapToObj(i -> IMAGE_DIR + slug + "-" + i + ".png")
        .toList();
  }

  public static List<String> imageUrlsForStored(String imageUrl) {
    if (imageUrl == null || imageUrl.isBlank()) {
      return List.of();
    }
    String suffix = "-1.png";
    if (imageUrl.startsWith(IMAGE_DIR) && imageUrl.endsWith(suffix)) {
      String slug = imageUrl.substring(IMAGE_DIR.length(), imageUrl.length() - suffix.length());
      if (!slug.isBlank() && !slug.contains("/") && !slug.contains("\\")) {
        return imageUrls(slug);
      }
    }
    return List.of(imageUrl);
  }

  public static int sortIndex(String designName) {
    for (int i = 0; i < ITEMS.size(); i++) {
      if (ITEMS.get(i).designName().equals(designName)) {
        return i;
      }
    }
    return ITEMS.size();
  }

  public record Item(
      BaseProduct baseProduct,
      String designName,
      String concept,
      String slug,
      String color,
      String size,
      int stock,
      int price) {

    public String primaryImageUrl() {
      return LabEditionCatalog.primaryImageUrl(slug);
    }
  }
}
