function order() {
  function createItem() {
    return {
      product: "",
      price: "",
      quantity: "",
      get amount() {
        return this.price * this.quantity;
      }
    };
  }

  return {
    items: [createItem()],
    addItem() {
      this.items.push(createItem());
    },
    editItem(index, price) {
      const item = this.items[index];
      item.price = price;
      if (item.quantity === "") item.quantity = 1;
    },
    removeItem(index) {
      this.items.length > 1 && this.items.splice(index, 1);
    },
    get totalPrice() {
      return this.items
        .map(item => item.amount)
        .reduce((prev, next) => prev + next);
    }
  };
}