# Initial Catalog and Verification

T03 seeds 20 ingredient identities, 10 aliases, two retailers, 12 real products, and 12 reviewed mappings. These are curated records, not fictional test fixtures. Product pages were reviewed on **22 October 2026** using the retailer's published identity, description, and pack details. This is a dated page review, not a physical inspection, availability guarantee, or allergen certification.

## Product Sources

Each link is the source for the stored name, pack label, and ingredient-form mapping. Review dates are stored in `product.verified_on` and `ingredient_product_mapping.reviewed_on`.

| Retailer | Ingredient code | Product source | Normalized pack |
| --- | --- | --- | --- |
| tesco | `white-rice-dry` | [Tesco Easy Cook Long Grain Rice 1Kg](https://www.tesco.ie/shop/en-IE/products/254877356) | 1000 G |
| tesco | `wheat-pasta-dry` | [Tesco Short Spaghetti Pasta 500G](https://www.tesco.ie/shop/en-IE/products/254878401) | 500 G |
| tesco | `tomato-canned` | [Tesco Italian Finely Chopped Tomatoes 400G](https://www.tesco.ie/shop/en-IE/products/254757959) | 400 G |
| tesco | `onion-fresh` | [Tesco Brown Onions Loose](https://www.tesco.ie/shop/en-IE/products/253557034) | Unknown fixed pack; manual checking |
| tesco | `carrot-fresh` | [Tesco Carrot Bag 500G](https://www.tesco.ie/shop/en-IE/products/294329565) | 500 G |
| tesco | `milk-whole` | [Tesco Fresh Milk1ltr](https://www.tesco.ie/shop/en-IE/products/292769732) | 1000 ML |
| dunnes | `white-rice-dry` | [Dunnes Easy Cook Rice 1kg](https://www.dunnesstoresgrocery.com/product/dunnes-easy-cook-rice-1kg-id-100742265) | 1000 G |
| dunnes | `wheat-pasta-dry` | [Dunnes Stores Spaghetti 500g](https://www.dunnesstoresgrocery.com/product/dunnes-stores-spaghetti-500g-id-100884813) | 500 G |
| dunnes | `tomato-canned` | [Dunnes Stores My Family Favourites Chopped Tomatoes in Tomato Juice 400g](https://www.dunnesstoresgrocery.com/product/dunnes-stores-my-family-favourites-chopped-tomatoes-in-tomato-juice-400g-id-100806212) | 400 G |
| dunnes | `onion-fresh` | [Dunnes Stores My Family Favourites Onions 1kg](https://www.dunnesstoresgrocery.com/product/dunnes-stores-my-family-favourites-onions-1kg-id-100290685) | 1000 G |
| dunnes | `carrot-fresh` | [Dunnes Stores Organic Carrots 1kg](https://www.dunnesstoresgrocery.com/product/dunnes-stores-organic-carrots-1kg-100704661) | 1000 G |
| dunnes | `milk-whole` | [Dunnes Stores Irish Whole Milk 1 Litre](https://www.dunnesstoresgrocery.com/product/dunnes-stores-irish-whole-milk-1-litre-id-100806906) | 1000 ML |

## Review Decisions

- Rice is dry parboiled white rice, not cooked rice. Specialist rice varieties require separate identities when a recipe needs them.
- Pasta is dried durum-wheat spaghetti, not fresh or rice pasta. This generic identity permits both shapes; shape-specific recipes need more specific identities.
- Canned tomatoes include their juice. The 400 g represents the entire tin, not drained tomato weight. Fresh tomatoes have a separate, unmapped identity.
- Tesco onions are loose and sold by weight. Estimated item weights do not establish a fixed pack: normalized quantity and unit remain null. Dunnes onions have a measured 1 kg pack; the number of onions is unknown.
- Carrots are fresh and unprepared. Dunnes organic carrots were a suitable verified single-pack source; this does not claim exhaustive discovery of the retailer's smallest pack.
- Both milk products are whole cow's milk, not skimmed or plant milk.
- Fourteen ingredient identities intentionally have no verified product mapping yet. The later shopping flow must preserve them as unmatched.

This small seed favors suitable own-brand single packs where verified; it is not a search for every alternative or the absolute smallest pack. Loose Tesco onions demonstrate an unknown fixed pack. No prices, stock claims, or fabricated retailer URLs are stored.

## Maintaining the Catalog

Before demos and when problems are reported, open the source and confirm the identity, form, pack label, usable quantity, and URL. A working URL alone does not establish suitability. Automated crawling and a recurring review schedule are outside T03.

Use a **new Flyway migration** for corrections, updating verification and review dates only after checking sources. Never edit an applied migration. Preserve stable ingredient codes and distinguish materially different forms. Keep fictional products exclusively in transactional test fixtures.

T14 targets approximately 40 ingredients and 60 verified products. No French or Italian cuisine choices are introduced here. Larger, frequently changing catalogs may later use a validated import workflow.
