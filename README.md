# SarvaMart

An **e‑commerce Android application** that lets users seamlessly browse products, add them to a cart, and place secure orders – all in a lightweight, Firebase‑powered experience.

---

## Key Features

| Category               | Highlights                                                                                                                   |
| ---------------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| **Product Catalog**    | • Infinite scrolling search• Product keywords & tags• Cloudinary‑hosted images                                               |
| **Cart & Checkout**    | • Real‑time cart badge counter• Quantity validation via Firestore transactions• Address book with single‑default enforcement |
| **Authentication**     | • Firebase Email/Password• Auth‑aware BottomNavigationView that auto‑redirects to Login                                      |
| **Order Management**   | • Snapshot of shipping address stored with every order• User order history stored under `/users/{uid}/orders`                |
| **Search History**     | • Per‑user search history capped at 10 entries with quick‑delete                                                             |
| **Offline Resilience** | • Firestore local cache • Glide image caching                                                                                |

---

## Tech Stack

| Layer        | Technology                                                  |
| ------------ | ----------------------------------------------------------- |
| **Language** | Java 17                                                     |
| **Data**     | Firebase Firestore (NoSQL)                                  |
| **Auth**     | Firebase Authentication                                     |
| **Storage**  | Cloudinary (product images)                                 |
| **UI**       | Material3, RecyclerView, View Binding, Navigation Component |
| **Misc**     | Glide, Gson, CircularProgressIndicator                      |

---

## Architecture at a Glance

```txt
MainActivity
 ├── NavHostFragment
 │    ├── HomeFragment
 │    ├── SearchFragment
 │    ├── CartFragment
 │    └── AccountFragment
 └── ViewModels
      ├── LoginViewModel  (auth + redirect flag)
      └── CartViewModel   (badge LiveData)
```

---

## Getting Started

### Prerequisites

- Android Studio Iguana (AGP 8.5+)
- JDK 17
- A Firebase project with Firestore & Authentication enabled
- A Cloudinary account (free tier is fine)

### Setup Steps

1. **Clone the repo**
   ```bash
   git clone https://github.com/your‑org/sarvamart.git
   cd sarvamart
   ```
2. **Configure Firebase**\
   *Download* `google-services.json` from the Firebase console and place it in `app/`.
3. **Set Cloudinary keys**\
   Edit `local.properties` (not committed) and add:
   ```properties
   CLOUDINARY_CLOUD_NAME=...
   CLOUDINARY_API_KEY=...
   CLOUDINARY_API_SECRET=...
   ```
4. **Sync & Run**\
   *Sync Gradle*, connect a device/emulator, and **Run** ▶️.

---

## Download

Grab the latest release **APK** directly:

[**Download SarvaMart v1.0.0**](https://github.com/your-org/sarvamart/releases/latest/download/sarvamart.apk)

*(link will auto‑resolve to the most recent build)*

---


## Contributing

Have an idea or bug fix? **PRs are welcome!**

1. Fork ➜ create a branch ➜ commit ➜ open PR.
2. Follow the Kotlin/Java style guide & write concise commit messages.
3. For UI changes, attach before/after screenshots.

---

## License

```
MIT License
Copyright (c) 2025 Sujay Kumar
```
