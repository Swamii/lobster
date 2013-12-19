'use strict'

api = angular.module 'app.services', ['restangular']

api.factory 'Api', ['Restangular', (Restangular) ->
  Restangular.withConfig (RestangularConfigurer) ->
    RestangularConfigurer.setBaseUrl('/api')
]

api.factory 'UserService', ['USER', (USER) ->
  isLoggedIn: ->
    USER and USER.email
  isAdmin: ->
    USER and USER.email and USER.isAdmin
  email: USER.email
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
        # Restangular's copy method sadly doesn't seem to trigger $watch-updates
        angular.copy incCart, cart

  setCart()

  buy = (product) ->
    Api.all('cart').post(id: product.id).then (cartItem) ->
        cart.push cartItem
        $rootScope.$broadcast 'itemAdded'
      , (error) ->
        FlashService.show error

  buyStatus = (product) ->
    if not UserService.isLoggedIn()
      return "login"
    else if ownedByUser product
      return "owned"
    else
      return "buy"

  ownedByUser = (product) ->
    owned = no
    angular.forEach cart, (item) ->
      if item.product.id is product.id
        owned = yes
    owned

  cart: cart
  reset: ->
    setCart()
  add: (product) ->
    buy(product)
  remove: (item) ->
    remove(item)
  buyStatus: (product) ->
    buyStatus(product)
]

##
## NOT USED, use angular's $broadcast or $emit with $on instead -> ##
##
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
