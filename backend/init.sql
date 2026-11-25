-- CreateTable
CREATE TABLE `users` (
    `id` VARCHAR(191) NOT NULL,
    `username` VARCHAR(191) NOT NULL,
    `passwordHash` VARCHAR(191) NOT NULL,
    `fullName` VARCHAR(191) NOT NULL,
    `role` ENUM('KASIR', 'ADMIN') NOT NULL DEFAULT 'KASIR',
    `isActive` BOOLEAN NOT NULL DEFAULT true,
    `createdAt` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updatedAt` DATETIME(3) NOT NULL,

    UNIQUE INDEX `users_username_key`(`username`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `tables` (
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `number` VARCHAR(191) NOT NULL,
    `area` VARCHAR(191) NOT NULL DEFAULT 'Indoor',
    `capacity` INTEGER NOT NULL DEFAULT 4,
    `status` ENUM('KOSONG', 'BARU_PESAN', 'TERISI_BELUM_BAYAR', 'TERISI_LUNAS') NOT NULL DEFAULT 'KOSONG',
    `activeSince` DATETIME(3) NULL,
    `currentOrderId` VARCHAR(191) NULL,
    `createdAt` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updatedAt` DATETIME(3) NOT NULL,

    UNIQUE INDEX `tables_number_key`(`number`),
    UNIQUE INDEX `tables_currentOrderId_key`(`currentOrderId`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `categories` (
    `id` VARCHAR(191) NOT NULL,
    `name` VARCHAR(191) NOT NULL,
    `slug` VARCHAR(191) NOT NULL,
    `displayOrder` INTEGER NOT NULL DEFAULT 0,
    `createdAt` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updatedAt` DATETIME(3) NOT NULL,

    UNIQUE INDEX `categories_name_key`(`name`),
    UNIQUE INDEX `categories_slug_key`(`slug`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `menu_items` (
    `id` VARCHAR(191) NOT NULL,
    `name` VARCHAR(191) NOT NULL,
    `categoryId` VARCHAR(191) NOT NULL,
    `price` BIGINT NOT NULL,
    `image` VARCHAR(500) NULL,
    `description` VARCHAR(500) NOT NULL DEFAULT '',
    `isAvailable` BOOLEAN NOT NULL DEFAULT true,
    `filters` JSON NULL,
    `createdAt` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updatedAt` DATETIME(3) NOT NULL,

    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `orders` (
    `id` VARCHAR(191) NOT NULL,
    `orderNumber` VARCHAR(191) NOT NULL,
    `tableId` INTEGER NOT NULL,
    `status` ENUM('MENUNGGU_KONFIRMASI', 'DIPROSES', 'DISAJIKAN', 'SELESAI', 'DIBATALKAN') NOT NULL DEFAULT 'MENUNGGU_KONFIRMASI',
    `paymentBillStatus` ENUM('BELUM_BAYAR', 'LUNAS') NOT NULL DEFAULT 'BELUM_BAYAR',
    `paymentMethod` ENUM('BELUM_DIPILIH', 'CASH', 'TRANSFER') NOT NULL DEFAULT 'BELUM_DIPILIH',
    `orderSource` ENUM('EMENU', 'KASIR') NOT NULL DEFAULT 'EMENU',
    `customerNotes` VARCHAR(500) NULL,
    `subtotal` BIGINT NOT NULL DEFAULT 0,
    `tax` BIGINT NOT NULL DEFAULT 0,
    `total` BIGINT NOT NULL DEFAULT 0,
    `confirmedAt` DATETIME(3) NULL,
    `paidAt` DATETIME(3) NULL,
    `createdAt` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updatedAt` DATETIME(3) NOT NULL,

    UNIQUE INDEX `orders_orderNumber_key`(`orderNumber`),
    INDEX `orders_createdAt_idx`(`createdAt`),
    INDEX `orders_status_idx`(`status`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `order_items` (
    `id` VARCHAR(191) NOT NULL,
    `orderId` VARCHAR(191) NOT NULL,
    `menuItemId` VARCHAR(191) NULL,
    `name` VARCHAR(191) NOT NULL,
    `categoryName` VARCHAR(191) NOT NULL,
    `unitPrice` BIGINT NOT NULL,
    `quantity` INTEGER NOT NULL,
    `note` VARCHAR(255) NULL,
    `subtotal` BIGINT NOT NULL,
    `createdAt` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `service_requests` (
    `id` VARCHAR(191) NOT NULL,
    `tableId` INTEGER NOT NULL,
    `type` ENUM('PANGGIL_PELAYAN', 'MINTA_BILL', 'MINTA_ALAT_MAKAN', 'LAINNYA') NOT NULL DEFAULT 'PANGGIL_PELAYAN',
    `message` VARCHAR(500) NULL,
    `status` ENUM('MENUNGGU', 'DIKONFIRMASI', 'SELESAI') NOT NULL DEFAULT 'MENUNGGU',
    `responseMessage` VARCHAR(500) NULL,
    `confirmedAt` DATETIME(3) NULL,
    `createdAt` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updatedAt` DATETIME(3) NOT NULL,

    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- CreateTable
CREATE TABLE `reviews` (
    `id` VARCHAR(191) NOT NULL,
    `tableId` INTEGER NOT NULL,
    `rating` INTEGER NOT NULL,
    `aspects` JSON NULL,
    `comment` VARCHAR(1000) NULL,
    `createdAt` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- AddForeignKey
ALTER TABLE `tables` ADD CONSTRAINT `tables_currentOrderId_fkey` FOREIGN KEY (`currentOrderId`) REFERENCES `orders`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `menu_items` ADD CONSTRAINT `menu_items_categoryId_fkey` FOREIGN KEY (`categoryId`) REFERENCES `categories`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `orders` ADD CONSTRAINT `orders_tableId_fkey` FOREIGN KEY (`tableId`) REFERENCES `tables`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `order_items` ADD CONSTRAINT `order_items_orderId_fkey` FOREIGN KEY (`orderId`) REFERENCES `orders`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `order_items` ADD CONSTRAINT `order_items_menuItemId_fkey` FOREIGN KEY (`menuItemId`) REFERENCES `menu_items`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `service_requests` ADD CONSTRAINT `service_requests_tableId_fkey` FOREIGN KEY (`tableId`) REFERENCES `tables`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `reviews` ADD CONSTRAINT `reviews_tableId_fkey` FOREIGN KEY (`tableId`) REFERENCES `tables`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;
