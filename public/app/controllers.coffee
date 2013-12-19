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
      console.error "error error"

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
      console.error "error error putting"
]

app.controller 'GenericFormCtrl', ['$scope', ($scope) ->

]

app.controller 'BrowseCtrl', ['$scope', 'FlashService', 'Api', 'page', 'Utils', 'CartService', '$modal', ($scope, FlashService, Api, page, Utils, CartService, $modal) ->
  $scope.page = page
  $scope.pageRange = null
  $scope.filters =
    page: 1
    size: 10
    sort: 'name'
    order: 'asc'
    filter: ''
    category: ''

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
    $scope.buttonText = 'Update'

  setValues(page)

  $scope.goTo = (pageNum) ->
    if pageNum isnt $scope.page.pageIndex
      $scope.filters.page = pageNum
      $scope.update()

  $scope.buy = CartService.add
  $scope.buyStatus = CartService.buyStatus

  $scope.showCart = ->
    $scope.$emit('showCart')

  $scope.showDetail = (product) ->
    modalInstance = $modal.open
      templateUrl: '/assets/angular/product-modal.html'
      controller: 'ProductDetailCtrl'
      resolve:
        product: ->
          product

    modalInstance.result.then ->
      # if there's a result, that means go to cart
      $scope.showCart()
]

app.controller 'ProductDetailCtrl', ['$scope', '$modalInstance', 'product', 'CartService', ($scope, $self, product, CartService) ->
  $scope.product = product
  $scope.buyStatus = CartService.buyStatus
  $scope.buy = CartService.add

  $scope.showCart = ->
    $self.close()

  $scope.close = ->
    $self.dismiss('cancel')
]

app.controller 'CartCtrl', ['$scope', 'CartService', 'Api', '$modal', '$rootScope', '$timeout', ($scope, CartService, Api, $modal, $rootScope, $timeout) ->
  $scope.cart = CartService.cart
  $scope.doFlash = false

  $scope.showCart = ->
    $modal.open
      templateUrl: '/assets/angular/cart-modal.html'
      controller: 'CartModalCtrl'
      resolve:
        cart: ->
          CartService.cart

  $scope.$on 'itemAdded', (event, obj) ->
    $scope.doFlash = true
    $timeout ->
        $scope.doFlash = false
      , 2000

  $rootScope.$on 'showCart', (event, obj) ->
    $scope.showCart()
]

app.controller 'CartModalCtrl', ['$scope', '$modalInstance', 'Api', 'cart', 'Utils', ($scope, $modalInstance, Api, cart, Utils) ->
  $scope.cart = cart
  $scope.editable = {}

  $scope.close = ->
    $modalInstance.dismiss('cancel')

  $scope.update = ->
    # This should be in CartService but i need to reset $scope.editable. If I reset it directly after the CartService call
    # the field will flicker back to a span with the old value before updating. Not gut.
    $scope.errors = null
    $scope.editable.put().then (item) ->
        idx = Utils.indexById $scope.cart, item.id
        $scope.cart.splice idx, 1, item
        $scope.editable = {}
      , (result) ->
        $scope.errors = result.data
        console.error "error updating #{item}"
        $scope.editable = {}

  $scope.openForEdit = (index) ->
    $scope.editable = Api.copy $scope.cart[index]

  $scope.remove = (index) ->
    item = $scope.cart[index]
    item.remove().then ->
        $scope.cart.splice index, 1
      , ->
        console.error("error removing #{item} from cart")
]

app.controller 'CheckoutCtrl', ['$scope', 'CartService', ($scope, CartService) ->
  $scope.cart = CartService.cart

]