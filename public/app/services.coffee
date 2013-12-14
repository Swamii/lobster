'use strict'

api = angular.module 'app.services', ['restangular']

api.factory 'Api', ['Restangular', (Restangular) ->
  Restangular.withConfig (RestangularConfigurer) ->
    RestangularConfigurer.setBaseUrl('/api')
]

api.factory 'UserService', ['USER', (USER) ->
  isLoggedIn: ->
    !!USER
  email: USER
]

api.factory 'FlashService', ['$rootScope', ($rootScope) ->
  # type maps to bootstraps alert classes (danger/info/warning/success)
  show: (message, type = "danger") ->
    $rootScope.flash = message
    $rootScope.flashClass = type
  clear: ->
    $rootScope.flash = ''
    $rootScope.flashClass = ''
]

api.factory 'CartService', ['Api', 'UserService', '$rootScope', 'FlashService', (Api, UserService, $rootScope, FlashService) ->
  cart = []
  setCart = ->
    if UserService.isLoggedIn()
      Api.all('cart').getList().then (incCart) ->
        console.log incCart
        angular.copy incCart, cart

  buy = (product) ->
    cart.post(id: product.id).then (cartItem) ->
        cart.cartItems.push cartItem
        $rootScope.$broadcast('itemAdded')
      , (error) ->
        FlashService.show error

  remove = (item) ->
    console.log item

  setCart()

  cart: cart
  reset: ->
    setCart()
  add: (product) ->
    buy(product)
  remove: (item) ->
    remove(item)
]

api.factory 'SubService', ->
  cache = {}

  publish: (topic, args) ->
    cache[topic] and angular.forEach cache[topic], (func) ->
      func.apply null, args or []
  subscribe: (topic, callback) ->
    cache[topic] = [] if not cache[topic]
    cache[topic].push callback
    [topic, callback]
  unsubscribe: (handle) ->
    t = handle[0]
    cache[t] and angular.forEach cache[t] (val, idx) ->
      cache[t].splice idx, 1 if val is handle[1]


api.factory 'DudeService', ->
  dudes = []

  dudes: dudes
  add: (newDude) ->
    dudes.push newDude
    dudes
  remove: (remDude) ->
    angular.forEach dudes, (dude, idx) ->
      if remDude.id is dude.id
        dudes.splice(idx, 1)
    dudes