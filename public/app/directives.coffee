app = angular.module 'app.directives', []

app.directive 'pwCheck', ->
    require: 'ngModel'
    link: (scope, elem, attrs, ctrl) ->
      firstPwd = '#' + attrs.pwCheck
      elem.add(firstPwd).on 'keyup', ->
        scope.$apply ->
          v = elem.val() is $(firstPwd).val()
          ctrl.$setValidity 'pwdmatch', v

app.directive 'onEnter', ->
  (scope, element, attrs) ->
    element.bind "keydown keypress", (event) ->
      if event.which is 13
        scope.$apply ->
          scope.$eval(attrs.onEnter)
          event.preventDefault()

app.directive 'lowestNumber', ->
  restrict: 'A'
  require: 'ngModel'
  link: (scope, element, attrs, ctrl) ->
    if attrs.lowestNumber
      lowestNumber = parseInt attrs.lowestNumber, 10
    else
      lowestNumber = 1
    scope.$watch attrs.ngModel, (newVal, oldVal) ->
      if newVal isnt undefined
        num = parseInt newVal, 10
        if isNaN(num) or num < lowestNumber
          ctrl.$setViewValue lowestNumber
          ctrl.$render()

app.directive 'initial', ->
  restrict: 'A'
  controller: ['$scope', '$element', '$attrs', '$parse', ($scope, $element, $attrs, $parse) ->
    val = $attrs.initial or $attrs.value or $element.text()
    getter = $parse $attrs.ngModel
    setter = getter.assign
    setter $scope, val
  ]