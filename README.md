# The idea of the project and my contribution

“My Fridge” is a mobile companion that helps households keep track of what is inside the refrigerator, avoid food waste and instantly cook something tasty from what is already there. I delivered the project end‑to‑end: from UX‑flow and clean, modular MVVM architecture to an AI‑powered Android application and family‑sharing back‑end

[english](https://github.com/winipux21/CyberReferent/blob/main/README.md) [русский](https://github.com/winipux21/CyberReferent/blob/main/README/ru.md)

My area of responsibility included
* Architecture – Jetpack Compose UI + Navigation, Repository/Use‑Case layers, Room for offline‑first storage, Firebase Realtime DB for cloud sync, and a dedicated AI gateway
* Core features – product & shopping‑list CRUD, barcode/photo recognition, recipe generator, family work‑space and role management
* AI integration – OpenAI Vision endpoint (products from fridge photo)  and DeepSeek LLM for context‑aware recipe generation  
* Security & auth – Google Sign‑In with refresh‑token rotation, encrypted SharedPreferences for local secrets
* Real‑time collaboration – family recipes / products synchronised through Firebase listeners 
* CI & QA – GitHub Actions pipeline, unit / UI tests (JUnit5 + Compose‑UITest) covering 80 % of logic

## Project concept
* On‑boarding & main hub – after Google auth the user lands on the MainScreen with three tabs: Fridge, Shopping list, Recipes  
* Fridge tab – local Room database shows all items with quantity, unit and best‑before date; plus buttons to add manually or “via photo”  
* Shopping list – simple check‑list with swipe‑to‑delete and bulk status toggle  
* Recipe tab – select meal type / diet mode and let DeepSeek return 1–3 personalised recipes using only products still fresh  
* Family workspace – share fridge, lists or single recipe with relatives; owner can rename or delete the family space  
* Photo recognition – take or pick a picture, the app calls OpenAI, receives “[Молоко 1 шт СГ 7 дней]…” and writes items straight to the DB  

Data flow: local entities (Room) ↔ sync layer ↔ Firebase so every change appears on all phones instantly; AI services are routed through a secure gateway, no keys live in the APK

## Project demonstration

* Refrigerator + side menu. The main tab shows the current products with the quantity and expiration date; at the top is the "Add via photo" button, at the bottom is the FAB "+" for manual input. The dash menu on the avatar provides quick access to your profile, personal/family recipes, and logout.
* Recipe card. A detailed recipe from the saved ones: the name, a compact list of ingredients and step—by-step instructions on one card - you can immediately start cooking without switching between screens.
* Profile. The user changes the avatar with a single tap, edits the nickname and saves it to Firebase; a separate button opens the setting of the product reminder time.
* Family space. The "Family Details" screen shows the ID code for invitations, common products/recipes/shopping list and gives the owner the right to rename or delete the family, as well as to search for individual entities with one click.

<img width="392" height="832" alt="Screenshot_31" src="https://github.com/user-attachments/assets/ad1d81a3-6ef0-44be-9016-43a7cc3da950" /> <img width="387" height="830" alt="Screenshot_32" src="https://github.com/user-attachments/assets/d2811519-62c7-4652-a38f-65aae626b239" /> <img width="393" height="824" alt="Screenshot_33" src="https://github.com/user-attachments/assets/eafb5537-8c71-40dd-839c-e61638cacb12" /> <img width="385" height="828" alt="Screenshot_34" src="https://github.com/user-attachments/assets/2124f041-a0b8-426d-a555-38151e31c3d5" />

