-- Real curated catalog; provenance and review notes: backend/CATALOG.md.
INSERT INTO ingredient (code, name, form) VALUES
    ('white-rice-dry', 'White rice', 'dry'),
    ('wheat-pasta-dry', 'Wheat pasta', 'dry'),
    ('rice-noodles-dry', 'Rice noodles', 'dry'),
    ('potato-fresh', 'Potatoes', 'fresh'),
    ('onion-fresh', 'Onions', 'fresh'),
    ('garlic-fresh', 'Garlic', 'fresh'),
    ('carrot-fresh', 'Carrots', 'fresh'),
    ('tomato-fresh', 'Tomatoes', 'fresh'),
    ('tomato-canned', 'Chopped tomatoes in juice', 'canned'),
    ('chickpea-canned', 'Chickpeas', 'canned'),
    ('chicken-breast-raw', 'Chicken breast', 'raw'),
    ('beef-mince-raw', 'Beef mince', 'raw'),
    ('egg-whole', 'Eggs', 'whole'),
    ('tofu-plain', 'Tofu', 'plain'),
    ('milk-whole', 'Milk', 'whole'),
    ('butter', 'Butter', 'solid'),
    ('olive-oil', 'Olive oil', 'liquid'),
    ('wheat-flour-plain', 'Wheat flour', 'plain'),
    ('soy-sauce', 'Soy sauce', 'liquid'),
    ('fish-sauce', 'Fish sauce', 'liquid');

INSERT INTO retailer (code, name, website_url) VALUES
    ('tesco', 'Tesco Ireland', 'https://www.tesco.ie'),
    ('dunnes', 'Dunnes Stores', 'https://www.dunnesstoresgrocery.com');

INSERT INTO ingredient_alias (ingredient_id, name)
SELECT i.id, a.name FROM (VALUES
    ('white-rice-dry', 'dry white rice'),
    ('wheat-pasta-dry', 'dried wheat pasta'),
    ('potato-fresh', 'fresh potato'),
    ('onion-fresh', 'brown onion'),
    ('carrot-fresh', 'fresh carrot'),
    ('tomato-canned', 'tinned chopped tomatoes'),
    ('chickpea-canned', 'tinned chickpeas'),
    ('chickpea-canned', 'canned garbanzo beans'),
    ('beef-mince-raw', 'ground beef'),
    ('wheat-flour-plain', 'all-purpose flour')
) AS a(code, name) JOIN ingredient i ON i.code = a.code;

INSERT INTO product (retailer_id, name, product_url, pack_label, pack_quantity, pack_unit, verified_on)
SELECT r.id, p.name, p.url, p.label, p.quantity, p.unit, DATE '2026-10-22'
FROM (VALUES
    ('tesco', 'Tesco Easy Cook Long Grain Rice 1Kg', 'https://www.tesco.ie/shop/en-IE/products/254877356', '1 kg', 1000, 'G'),
    ('tesco', 'Tesco Short Spaghetti Pasta 500G', 'https://www.tesco.ie/shop/en-IE/products/254878401', '500 g', 500, 'G'),
    ('tesco', 'Tesco Italian Finely Chopped Tomatoes 400G', 'https://www.tesco.ie/shop/en-IE/products/254757959', '400 g (including tomato juice)', 400, 'G'),
    ('tesco', 'Tesco Brown Onions Loose', 'https://www.tesco.ie/shop/en-IE/products/253557034', 'Loose; sold by weight', NULL, NULL),
    ('tesco', 'Tesco Carrot Bag 500G', 'https://www.tesco.ie/shop/en-IE/products/294329565', '500 g', 500, 'G'),
    ('tesco', 'Tesco Fresh Milk1ltr', 'https://www.tesco.ie/shop/en-IE/products/292769732', '1 litre', 1000, 'ML'),
    ('dunnes', 'Dunnes Easy Cook Rice 1kg', 'https://www.dunnesstoresgrocery.com/product/dunnes-easy-cook-rice-1kg-id-100742265', '1 kg', 1000, 'G'),
    ('dunnes', 'Dunnes Stores Spaghetti 500g', 'https://www.dunnesstoresgrocery.com/product/dunnes-stores-spaghetti-500g-id-100884813', '500 g', 500, 'G'),
    ('dunnes', 'Dunnes Stores My Family Favourites Chopped Tomatoes in Tomato Juice 400g', 'https://www.dunnesstoresgrocery.com/product/dunnes-stores-my-family-favourites-chopped-tomatoes-in-tomato-juice-400g-id-100806212', '400 g (including tomato juice)', 400, 'G'),
    ('dunnes', 'Dunnes Stores My Family Favourites Onions 1kg', 'https://www.dunnesstoresgrocery.com/product/dunnes-stores-my-family-favourites-onions-1kg-id-100290685', '1 kg', 1000, 'G'),
    ('dunnes', 'Dunnes Stores Organic Carrots 1kg', 'https://www.dunnesstoresgrocery.com/product/dunnes-stores-organic-carrots-1kg-100704661', '1 kg', 1000, 'G'),
    ('dunnes', 'Dunnes Stores Irish Whole Milk 1 Litre', 'https://www.dunnesstoresgrocery.com/product/dunnes-stores-irish-whole-milk-1-litre-id-100806906', '1 litre', 1000, 'ML')
) AS p(retailer_code, name, url, label, quantity, unit)
JOIN retailer r ON r.code = p.retailer_code;

INSERT INTO ingredient_product_mapping (ingredient_id, product_id, reviewed_on)
SELECT i.id, p.id, DATE '2026-10-22'
FROM (VALUES
    ('white-rice-dry', 'https://www.tesco.ie/shop/en-IE/products/254877356'),
    ('wheat-pasta-dry', 'https://www.tesco.ie/shop/en-IE/products/254878401'),
    ('tomato-canned', 'https://www.tesco.ie/shop/en-IE/products/254757959'),
    ('onion-fresh', 'https://www.tesco.ie/shop/en-IE/products/253557034'),
    ('carrot-fresh', 'https://www.tesco.ie/shop/en-IE/products/294329565'),
    ('milk-whole', 'https://www.tesco.ie/shop/en-IE/products/292769732'),
    ('white-rice-dry', 'https://www.dunnesstoresgrocery.com/product/dunnes-easy-cook-rice-1kg-id-100742265'),
    ('wheat-pasta-dry', 'https://www.dunnesstoresgrocery.com/product/dunnes-stores-spaghetti-500g-id-100884813'),
    ('tomato-canned', 'https://www.dunnesstoresgrocery.com/product/dunnes-stores-my-family-favourites-chopped-tomatoes-in-tomato-juice-400g-id-100806212'),
    ('onion-fresh', 'https://www.dunnesstoresgrocery.com/product/dunnes-stores-my-family-favourites-onions-1kg-id-100290685'),
    ('carrot-fresh', 'https://www.dunnesstoresgrocery.com/product/dunnes-stores-organic-carrots-1kg-100704661'),
    ('milk-whole', 'https://www.dunnesstoresgrocery.com/product/dunnes-stores-irish-whole-milk-1-litre-id-100806906')
) AS m(ingredient_code, product_url)
JOIN ingredient i ON i.code = m.ingredient_code
JOIN product p ON p.product_url = m.product_url;
