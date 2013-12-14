'use strict'

app = angular.module 'app', [
  'ui.router', 'ui.bootstrap', 'ngCookies', 'ngAnimate', 'app.services', 'app.controllers',
  'app.directives', 'app.utils', 'app.filters'
]

app.run ['$rootScope', '$rootElement', '$state', '$stateParams', 'FlashService', ($rootScope, $rootElement, $state, $stateParams, FlashService) ->
  $rootScope.$state = $state
  $rootScope.$stateParams = $stateParams
  $rootElement.off('click');
  $rootScope.$on '$stateChangeStart', ->
    FlashService.clear()
  $rootScope.$on '$startChangeError', ->
    FlashService.show "There was an routing error"
]

app.config ['$stateProvider', '$urlRouterProvider', 'RestangularProvider', ($stateProvider, $urlRouterProvider, RestangularProvider) ->
  # Restangular ->
  RestangularProvider.setResponseExtractor (response, operation, what, url) ->
    if operation is 'getList' and 'cartItems' of response
      newResponse = response.cartItems
      newResponse.id = response.id
      return newResponse
    return response

  # Routes ->
  #$urlRouterProvider.otherwise('/dude')

  $stateProvider
  .state 'product',
      url: '/browse'
      templateUrl: '/assets/angular/product-list.html'
      controller: 'BrowseCtrl'
      resolve:
        page: (Api) ->
          Api.one('product').get()
  .state 'home',
      url: '/home'
      templateUrl: '/assets/angular/home.html'
      # controller: 'HomeCtrl'
  .state 'dude',
      url: '/dude'
      templateUrl: '/assets/angular/dude-list.html'
      controller: 'DudeCtrl'
      resolve:
        dudes: (Api) ->
          Api.all('dude').getList()
  .state 'dude.detail',
      url: '/{id:[0-9]+}'
      templateUrl: 'assets/angular/dude-detail.html'
      controller: 'DudeDetailCtrl'
      resolve:
        dude: ($stateParams, Utils, dudes) ->
          Utils.fromId dudes, $stateParams.id
        dudes: (dudes) ->
            dudes
]
