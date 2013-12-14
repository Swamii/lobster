'use strict'

app = angular.module 'app.controllers', []

app.controller 'DudeCtrl', ['$scope', 'Api', 'dudes', ($scope, Api, dudes) ->
  $scope.dudes = dudes
  $scope.newDude = {}
  $scope.reversed = false

  $scope.toggleReversed = ->
    $scope.reversed = !$scope.reversed

  $scope.processForm = ->
    Api.all('dude').post($scope.newDude).then (dude) ->
      $scope.dudes.push dude
      $scope.newDude = {}
    , ->
      console.log "error error"

  $scope.selectDude = (selectedDude) ->
    angular.forEach $scope.dudes, (dude) ->
      dude.selected = dude is selectedDude

]

app.controller 'DudeDetailCtrl', ['$scope', 'Api', 'dudes', 'dude', 'Utils', ($scope, Api, dudes, selectedDude, Utils) ->
  $scope.dude = selectedDude

  closeEdit = ->
    $scope.editDude = {}
    $scope.isEdit = false

  closeEdit()
  $scope.closeEdit = closeEdit

  $scope.deleteDude = ->
    selectedDude.remove().then ->
      idx = Utils.indexById dudes, selectedDude
      dudes.splice idx, 1
      $scope.$state.go 'dude'

  $scope.openForEdit = ->
    $scope.isEdit = true
    $scope.editDude = Api.copy $scope.dude

  $scope.updateDude = ->
    editDude = $scope.editDude
    editDude.put().then (dude) ->
      idx = Utils.indexById dudes, dude
      dudes.splice idx, 1, dude
      $scope.dude = dude
      closeEdit()
    , ->
      console.log "error error putting"
]

app.controller 'SignupFormCtrl', ['$scope', ($scope) ->
    $scope.message = "Hello Form!"
]

app.controller 'BrowseCtrl', ['$scope', 'UserService', 'FlashService', 'Api', 'page', 'Utils', 'CartService', ($scope, UserService, FlashService, Api, page, Utils, CartService) ->
  $scope.page = page
  $scope.pageRange = null
  $scope.filters =
    page: 1
    size: 10
    sort: 'name'
    order: 'asc'
    filter: ''
    pType: ''

  $scope.update = ->
    $scope.buttonText = 'Loading...'
    Api.one('product').get($scope.filters).then (page) ->
      setValues(page)
    , ->
      FlashService.show "Error loading products"

  setValues = (page) ->
    $scope.page = page
    $scope.range = Utils.chunkIt(page.products, 4)
    FlashService.show "Showing #{page.products.length} results of #{page.totalRowCount}", "info"
    $scope.pageRange = if page.totalPages > 0 then [1..page.totalPages] else [1]
    $scope.buttonText = 'Update'

  setValues(page)

  $scope.goTo = (pageNum) ->
    if pageNum isnt $scope.page.pageIndex and pageNum in $scope.pageRange
      $scope.filters.page = pageNum
      $scope.update()

  $scope.buy = (product) ->
    CartService.add(product)

  $scope.buyStatus = (product) ->
    if not UserService.isLoggedIn()
      return "login"
    else if ownedByUser product
      return "owned"
    else
      return "buy"

  ownedByUser = (product) ->
    owned = no
    angular.forEach CartService.cart.cartItems, (item) ->
      if item.product.id is product.id
        owned = yes
    owned

]

app.controller 'CartCtrl', ['$scope', 'CartService', 'Api', '$modal', ($scope, CartService, Api, $modal) ->
  $scope.cart = CartService.cart
  console.log $scope.cart

  $scope.showCart = ->
    modalInstance = $modal.open
      templateUrl: '/assets/angular/cart-modal.html'
      controller: 'CartModalCtrl'
      resolve:
        cart: ->
          CartService.cart
]

app.controller 'CartModalCtrl', ['$scope', '$modalInstance', 'Api', 'cart', ($scope, $modalInstance, Api, cart) ->
  console.log cart
  $scope.cart = Api.copy cart

  $scope.close = ->
    $modalInstance.dismiss('cancel')

  $scope.update = (index) ->
    item = $scope.cart.cartItems[index]
    console.log $scope.cart
    $scope.cart.customPUT(item, '')

  $scope.remove = (index) ->
    item = $scope.cart.cartItems[index]
    console.log $scope.cart
    item.remove().then (newCart) ->
      $scope.cart = newCart

]