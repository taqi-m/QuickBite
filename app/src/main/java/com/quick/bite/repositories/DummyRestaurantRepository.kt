package com.quick.bite.repositories

import com.quick.bite.models.Product
import com.quick.bite.models.Restaurant

object DummyRestaurantRepository {

    fun getRestaurants(): List<Restaurant> {
        return listOf(
            Restaurant(
                id = 1,
                name = "Tokyo Umami Kitchen",
                category = "JAPANESE - $$$",
                rating = "4.8 *",
                deliveryTime = "24m",
                deliveryFee = "FREE"
            ),
            Restaurant(
                id = 2,
                name = "Crumb & Cream Bakery",
                category = "BAKERY - $$",
                rating = "4.7 *",
                deliveryTime = "31m",
                deliveryFee = "$1.99"
            ),
            Restaurant(
                id = 3,
                name = "Royal Spice Tandoor",
                category = "INDIAN - $$",
                rating = "4.6 *",
                deliveryTime = "29m",
                deliveryFee = "$1.49"
            )
        )
    }

    fun getProductsForRestaurant(restaurantId: Int): List<Product> {
        return when (restaurantId) {
            2 -> listOf(
                Product(
                    id = 201,
                    restaurantId = 2,
                    type = "Best Sellers",
                    name = "Belgian Chocolate Cake Slice",
                    description = "Moist chocolate sponge layered with rich ganache.",
                    price = "$5.80",
                    rating = "4.8 (162)",
                    isVeg = true
                ),
                Product(
                    id = 202,
                    restaurantId = 2,
                    type = "Best Sellers",
                    name = "Glazed Ring Donut",
                    description = "Freshly fried donut with vanilla sugar glaze.",
                    price = "$2.25",
                    rating = "4.7 (221)",
                    isVeg = true
                ),
                Product(
                    id = 203,
                    restaurantId = 2,
                    type = "Beverages",
                    name = "Iced Caramel Latte",
                    description = "Cold espresso with milk, caramel syrup, and foam.",
                    price = "$4.50",
                    rating = "4.5 (94)",
                    isVeg = true
                ),
                Product(
                    id = 204,
                    restaurantId = 2,
                    type = "Combos",
                    name = "Cake + Coffee Combo",
                    description = "One cake slice with a small hot cappuccino.",
                    price = "$8.25",
                    rating = "4.6 (51)",
                    isVeg = true
                ),
                Product(
                    id = 205,
                    restaurantId = 2,
                    type = "Desserts",
                    name = "Blueberry Cheesecake Jar",
                    description = "Creamy cheesecake mousse layered with blueberry compote.",
                    price = "$4.95",
                    rating = "4.7 (70)",
                    isVeg = true
                )
            )

            3 -> listOf(
                Product(
                    id = 301,
                    restaurantId = 3,
                    type = "Best Sellers",
                    name = "Chicken Biryani",
                    description = "Fragrant basmati rice with spicy marinated chicken.",
                    price = "$13.00",
                    rating = "4.8 (190)",
                    isVeg = false
                ),
                Product(
                    id = 302,
                    restaurantId = 3,
                    type = "Best Sellers",
                    name = "Paneer Tikka",
                    description = "Chargrilled cottage cheese cubes with masala spices.",
                    price = "$10.50",
                    rating = "4.7 (110)",
                    isVeg = true
                ),
                Product(
                    id = 303,
                    restaurantId = 3,
                    type = "Sides",
                    name = "Butter Naan",
                    description = "Soft tandoor-baked flatbread brushed with butter.",
                    price = "$2.99",
                    rating = "4.6 (132)",
                    isVeg = true
                ),
                Product(
                    id = 304,
                    restaurantId = 3,
                    type = "Combos",
                    name = "Biryani Family Combo",
                    description = "Two biryanis, raita, salad, and two soft drinks.",
                    price = "$24.50",
                    rating = "4.7 (89)",
                    isVeg = false
                ),
                Product(
                    id = 305,
                    restaurantId = 3,
                    type = "Beverages",
                    name = "Sweet Mango Lassi",
                    description = "Classic chilled yogurt drink blended with ripe mango.",
                    price = "$3.75",
                    rating = "4.6 (58)",
                    isVeg = true
                )
            )

            else -> listOf(
                Product(
                    id = 101,
                    restaurantId = 1,
                    type = "Best Sellers",
                    name = "Tonkotsu Ramen",
                    description = "Slow-cooked pork broth ramen with chashu and soft egg.",
                    price = "$12.90",
                    rating = "4.9 (241)",
                    isVeg = false
                ),
                Product(
                    id = 102,
                    restaurantId = 1,
                    type = "Best Sellers",
                    name = "Chicken Katsu Curry",
                    description = "Crispy chicken cutlet over rice with Japanese curry sauce.",
                    price = "$11.50",
                    rating = "4.7 (166)",
                    isVeg = false
                ),
                Product(
                    id = 103,
                    restaurantId = 1,
                    type = "Sides",
                    name = "Shrimp Tempura",
                    description = "Light crispy tempura shrimp with soy dipping sauce.",
                    price = "$8.50",
                    rating = "4.6 (122)",
                    isVeg = false
                ),
                Product(
                    id = 104,
                    restaurantId = 1,
                    type = "Combos",
                    name = "Sushi Combo Box",
                    description = "Salmon roll, tuna nigiri, cucumber maki, and miso soup.",
                    price = "$15.75",
                    rating = "4.8 (109)",
                    isVeg = false
                ),
                Product(
                    id = 105,
                    restaurantId = 1,
                    type = "Beverages",
                    name = "Matcha Iced Latte",
                    description = "Premium matcha whisked with milk and light sweetness.",
                    price = "$4.10",
                    rating = "4.5 (87)",
                    isVeg = true
                )
            )
        }
    }
}





